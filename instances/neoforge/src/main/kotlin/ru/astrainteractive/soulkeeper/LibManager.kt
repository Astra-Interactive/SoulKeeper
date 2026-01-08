package ru.astrainteractive.soulkeeper

import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.classloader.ClassLoaderHelper
import com.alessiodp.libby.classloader.SystemClassLoaderHelper
import com.alessiodp.libby.classloader.URLClassLoaderHelper
import com.alessiodp.libby.logging.adapters.LogAdapter
import cpw.mods.cl.ModuleClassLoader
import cpw.mods.modlauncher.Launcher
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path


/**
 * A runtime dependency manager for standalone java applications.
 */
class MyLibraryManager @JvmOverloads constructor(
    logAdapter: LogAdapter,
    dataDirectory: Path,
    directoryName: String = "lib",
    loader: URLClassLoader
) : LibraryManager(logAdapter, dataDirectory, directoryName) {
    val paths = mutableSetOf<Path>()
    private val urlclassloader = URLClassLoaderHelper(loader,this)

    override fun addToClasspath(file: Path) {
        paths.add(file)
        urlclassloader.addToClasspath(file)

    }
}