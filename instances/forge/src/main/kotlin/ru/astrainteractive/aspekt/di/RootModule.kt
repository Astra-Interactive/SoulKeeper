package ru.astrainteractive.aspekt.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.minecraftforge.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.coroutine.ForgeMainDispatcher
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import java.io.File


class RootModule {

    private val dataFolder by lazy {
        FMLPaths.CONFIGDIR.get()
            .resolve("AspeKt")
            .toAbsolutePath()
            .toFile()
            .also(File::mkdirs)
    }
    private val coreModule: CoreModule = CoreModule(
        dispatchers = object : KotlinDispatchers {
            override val Main: CoroutineDispatcher = ForgeMainDispatcher
            override val IO: CoroutineDispatcher = Dispatchers.IO
            override val Default: CoroutineDispatcher = Dispatchers.Default
            override val Unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        },
        dataFolder = dataFolder
    )

    private val soulsDaoModule = SoulsDaoModule.Default(
        dataFolder = coreModule.dataFolder,
        scope = coreModule.ioScope
    )

    private val serviceModule = ServiceModule(
        coreModule = coreModule,
        soulsDaoModule = soulsDaoModule,
        platformServiceModule = bukkitPlatformServiceModule
    )

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            coreModule.lifecycle,
            soulsDaoModule.lifecycle,
            serviceModule.lifecycle,
        )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            lifecycles.forEach(Lifecycle::onEnable)
        },
        onDisable = {
            lifecycles.forEach(Lifecycle::onDisable)
        },
        onReload = {
            lifecycles.forEach(Lifecycle::onReload)
        }
    )
}
