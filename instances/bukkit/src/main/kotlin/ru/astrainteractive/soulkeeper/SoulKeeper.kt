package ru.astrainteractive.soulkeeper

import com.alessiodp.libby.StandaloneLibraryManager
import com.alessiodp.libby.logging.adapters.JDKLogAdapter
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.libloader.api.LibLoader
import ru.astrainteractive.libloader.api.LibLoader.Library
import ru.astrainteractive.libloader.api.LibbyLibraryLoader
import ru.astrainteractive.libloader.async.VirtualThreadsJConcurrentExecutor
import ru.astrainteractive.soulkeeper.buildkonfig.BuildKonfig
import ru.astrainteractive.soulkeeper.di.RootModule

class SoulKeeper :
    LifecyclePlugin(),
    Logger by JUtiltLogger("SoulKeeper") {
    private val rootModule by lazy { RootModule(this) }

    override fun onEnable() {
        LibbyLibraryLoader(
            jConcurrentExecutor = VirtualThreadsJConcurrentExecutor,
            libraryManager = StandaloneLibraryManager(
                JDKLogAdapter(java.util.logging.Logger.getLogger("SoulKeeper-LibraryManager")),
                dataPath,
                "lib"
            )
        ).loadAll(
            libraries = BuildKonfig.DEPENDEPCIES.map(LibLoader::Library),
            repositories = listOf(
                LibLoader.Repository.MavenCentral,
                LibLoader.Repository.Url("https://repo.opencollab.dev/main/"),
                LibLoader.Repository.Url("https://repo.codemc.io/repository/maven-releases/")
            )
        )
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        rootModule.lifecycle.onDisable()
    }

    override fun onReload() {
        rootModule.lifecycle.onReload()
    }
}
