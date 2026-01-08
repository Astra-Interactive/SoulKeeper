package ru.astrainteractive.soulkeeper

import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.logging.LogLevel
import com.alessiodp.libby.logging.adapters.LogAdapter
import cpw.mods.cl.ModuleClassLoader
import java.lang.instrument.Instrumentation
import java.net.URL
import java.nio.file.Path
import java.util.jar.JarFile

/**
 * A runtime dependency manager for NeoForge using ByteBuddy's agent.
 * 
 * This implementation adds JARs directly to NeoForge's ModuleClassLoader
 * using ByteBuddy's Instrumentation API combined with reflection.
 */
class NeoForgeLibraryManager @JvmOverloads constructor(
    private val logger: LogAdapter,
    dataDirectory: Path,
    directoryName: String = "lib"
) : LibraryManager(logger, dataDirectory, directoryName) {
    
    private val instrumentation: Instrumentation? by lazy {
        logger.log(LogLevel.INFO, "Installing ByteBuddy agent...")
        try {
            // Use reflection to load ByteBuddy agent dynamically
            val byteBuddyAgentClass = Class.forName("net.bytebuddy.agent.ByteBuddyAgent")
            val installMethod = byteBuddyAgentClass.getMethod("install")
            installMethod.invoke(null) as? Instrumentation
        } catch (e: ClassNotFoundException) {
            logger.log(LogLevel.WARN, "ByteBuddy agent not found. Will use fallback approach.", e)
            null
        } catch (e: Exception) {
            logger.log(LogLevel.WARN, "Failed to install ByteBuddy agent. Will use fallback approach.", e)
            null
        }
    }
    
    private val modClassLoader: ClassLoader = Thread.currentThread().contextClassLoader
    
    init {
        logger.log(LogLevel.INFO, "Initializing NeoForgeLibraryManager")
        logger.log(LogLevel.INFO, "ClassLoader: ${modClassLoader::class.java.name}")
        
        // Try to initialize instrumentation
        instrumentation?.let {
            logger.log(LogLevel.INFO, "ByteBuddy agent installed successfully")
        } ?: logger.log(LogLevel.WARN, "ByteBuddy agent not available, using direct ModuleClassLoader manipulation")
    }

    override fun addToClasspath(file: Path) {
        try {
            val url = file.toUri().toURL()
            logger.log(LogLevel.INFO, "Adding JAR to classpath: $file")
            
            // Try ByteBuddy Instrumentation API first
            val added = instrumentation?.let { instr ->
                try {
                    // Add to bootstrap classloader search for maximum visibility
                    val jarFile = JarFile(file.toFile())
                    instr.appendToBootstrapClassLoaderSearch(jarFile)
                    logger.log(LogLevel.INFO, "Added to bootstrap classloader via Instrumentation: $file")
                    true
                } catch (e: Exception) {
                    logger.log(LogLevel.WARN, "Failed to add via Instrumentation: ${e.message}")
                    false
                }
            } ?: false
            
            // If Instrumentation failed or wasn't available, add directly to ModuleClassLoader
            if (!added && modClassLoader is ModuleClassLoader) {
                addToModuleClassLoader(modClassLoader, url)
            } else if (!added) {
                // Last resort: add to the classloader using reflection
                addViaReflection(modClassLoader, url)
            }
            
            logger.log(LogLevel.INFO, "Successfully added to classpath: $file")
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, "Failed to add $file to classpath", e)
            throw RuntimeException("Failed to add library to classpath: $file", e)
        }
    }
    
    private fun addToModuleClassLoader(loader: ModuleClassLoader, url: URL) {
        try {
            // Try to find and invoke addURL method on ModuleClassLoader
            val method = loader.javaClass.getDeclaredMethod("addURL", URL::class.java)
            method.isAccessible = true
            method.invoke(loader, url)
            logger.log(LogLevel.INFO, "Added to ModuleClassLoader via addURL: $url")
        } catch (e: Exception) {
            logger.log(LogLevel.WARN, "Failed to add via ModuleClassLoader.addURL: ${e.message}")
            addViaReflection(loader, url)
        }
    }
    
    private fun addViaReflection(loader: ClassLoader, url: URL) {
        try {
            // Last resort: try to access internal URLClassPath
            val ucpField = try {
                loader.javaClass.getDeclaredField("ucp")
            } catch (e: NoSuchFieldException) {
                loader.javaClass.superclass?.getDeclaredField("ucp")
            }
            
            ucpField?.let { field ->
                field.isAccessible = true
                val ucp = field.get(loader)
                
                if (ucp != null) {
                    val addURLMethod = ucp.javaClass.getDeclaredMethod("addURL", URL::class.java)
                    addURLMethod.isAccessible = true
                    addURLMethod.invoke(ucp, url)
                    logger.log(LogLevel.INFO, "Added via URLClassPath reflection: $url")
                } else {
                    throw RuntimeException("URLClassPath is null")
                }
            } ?: throw NoSuchFieldException("Could not find ucp field")
            
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, "All methods to add JAR failed", e)
            throw RuntimeException("Cannot add library to classpath: $url", e)
        }
    }
}
