package ru.astrainteractive.soulkeeper

import com.alessiodp.libby.logging.adapters.JDKLogAdapter
import kotlinx.coroutines.cancel
import net.neoforged.fml.common.Mod
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
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path
import javax.annotation.ParametersAreNonnullByDefault


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


    override fun onEnable() {
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        rootModule.lifecycle.onDisable()
        rootModule.coreModule.unconfinedScope.cancel()
    }

    override fun onReload() {
        rootModule.lifecycle.onReload()
    }

    init {
        println("Loader: ${ForgeBootstrap.loader}")
        println("ClassLoader: ${javaClass.classLoader}")
        println("ClassLoader: ${Thread.currentThread().contextClassLoader}")
        LibbyLibraryLoader(
            jConcurrentExecutor = VirtualThreadsJConcurrentExecutor,
            libraryManager = MyLibraryManager(
                JDKLogAdapter(java.util.logging.Logger.getLogger("SoulKeeper-LibraryManager")),
                dataFolder.toPath(),
                ".libraries",
                ForgeBootstrap.loader
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
    }
}
