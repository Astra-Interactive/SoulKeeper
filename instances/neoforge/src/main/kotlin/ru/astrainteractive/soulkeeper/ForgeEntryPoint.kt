package ru.astrainteractive.soulkeeper

import com.alessiodp.libby.logging.adapters.JDKLogAdapter
import kotlinx.coroutines.cancel
import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.lifecycle.ForgeLifecycleServer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.libloader.api.LibLoader
import ru.astrainteractive.libloader.api.LibbyLibraryLoader
import ru.astrainteractive.libloader.async.VirtualThreadsJConcurrentExecutor
import ru.astrainteractive.soulkeeper.buildkonfig.BuildKonfig
import ru.astrainteractive.soulkeeper.di.RootModule
import java.io.File

/**
 * Main entry point for the SoulKeeper NeoForge mod.
 * 
 * This class handles the lifecycle of the mod and loads runtime dependencies
 * using the NeoForgeLibraryManager which is compatible with NeoForge's ModuleClassLoader.
 */
class ForgeEntryPoint : ForgeLifecycleServer(),
    Logger by JUtiltLogger("SoulKeeper-ForgeEntryPoint"),
    Lifecycle {
    
    private val dataFolder by lazy {
        FMLPaths.CONFIGDIR.get()
            .resolve("SoulKeeper")
            .toAbsolutePath()
            .toFile()
            .also(File::mkdirs)
    }
    
    private val rootModule by lazy { RootModule(this, dataFolder) }

    init {
        info { "Initializing SoulKeeper for NeoForge" }
        info { "ClassLoader: ${javaClass.classLoader}" }
        info { "Context ClassLoader: ${Thread.currentThread().contextClassLoader}" }
        
        // Load runtime dependencies using NeoForgeLibraryManager
        try {
            LibbyLibraryLoader(
                jConcurrentExecutor = VirtualThreadsJConcurrentExecutor,
                libraryManager = NeoForgeLibraryManager(
                    JDKLogAdapter(java.util.logging.Logger.getLogger("SoulKeeper-LibraryManager")),
                    dataFolder.toPath(),
                    ".libraries"
                )
            ).loadAll(
                libraries = BuildKonfig
                    .DEPENDEPCIES
                    .map(LibLoader::Library),
                excludedTransitiveDependencies = BuildKonfig
                    .EXCLUDED_TRANSITIVE_DEPENDENCIES
                    .map(LibLoader::Dependency),
                repositories = BuildKonfig
                    .REPOSITORIES
                    .map(LibLoader.Repository::Url),
            )
            info { "Successfully loaded all runtime dependencies" }
        } catch (e: Exception) {
            error(e) { "Failed to load runtime dependencies" }
            throw RuntimeException("Failed to initialize SoulKeeper due to dependency loading failure", e)
        }
    }

    override fun onEnable() {
        info { "Enabling SoulKeeper" }
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        info { "Disabling SoulKeeper" }
        rootModule.lifecycle.onDisable()
        rootModule.coreModule.unconfinedScope.cancel()
    }

    override fun onReload() {
        info { "Reloading SoulKeeper" }
        rootModule.lifecycle.onReload()
    }
}
