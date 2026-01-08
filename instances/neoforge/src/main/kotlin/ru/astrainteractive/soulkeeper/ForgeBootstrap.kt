package ru.astrainteractive.soulkeeper

import com.alessiodp.libby.logging.adapters.JDKLogAdapter
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.libloader.api.LibLoader
import ru.astrainteractive.libloader.api.LibLoader.Dependency
import ru.astrainteractive.libloader.api.LibLoader.Library
import ru.astrainteractive.libloader.api.LibLoader.Repository.Url
import ru.astrainteractive.libloader.api.LibbyLibraryLoader
import ru.astrainteractive.libloader.async.VirtualThreadsJConcurrentExecutor
import ru.astrainteractive.soulkeeper.buildkonfig.BuildKonfig
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path

@Mod("soulkeeper")
class ForgeBootstrap {
    init {
        loader = PluginClassLoader(arrayOf(), javaClass.classLoader)
        val clazz = Class.forName(
            "ru.astrainteractive.soulkeeper.ForgeEntryPoint",
            true,
            loader
        )

        clazz.getDeclaredConstructor().newInstance() as ForgeEntryPoint
    }
    companion object {
        lateinit var loader: PluginClassLoader
    }
}