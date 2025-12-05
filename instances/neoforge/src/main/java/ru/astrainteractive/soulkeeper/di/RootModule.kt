package ru.astrainteractive.soulkeeper.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.di.ForgeEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.NeoForgePlatformServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import java.io.File

class RootModule(private val plugin: Lifecycle) {

    private val dataFolder by lazy {
        FMLPaths.CONFIGDIR.get()
            .resolve("SoulKeeper")
            .toAbsolutePath()
            .toFile()
            .also(File::mkdirs)
    }

    val coreModule: CoreModule by lazy {
        CoreModule(
            dataFolder = dataFolder,
            dispatchers = object : KotlinDispatchers {
                override val Main: CoroutineDispatcher by lazy {
                    TODO()
                }
                override val IO: CoroutineDispatcher = Dispatchers.IO
                override val Default: CoroutineDispatcher = Dispatchers.Default
                override val Unconfined: CoroutineDispatcher = Dispatchers.Unconfined
            }
        )
    }

    private val soulsDaoModule by lazy {
        SoulsDaoModule.Default(
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope
        )
    }
    private val forgePlatformServiceModule by lazy {
        NeoForgePlatformServiceModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule
        )
    }

    private val serviceModule by lazy {
        ServiceModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule,
            platformServiceModule = forgePlatformServiceModule
        )
    }
    private val forgeEventModule by lazy {
        ForgeEventModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule,
            effectEmitter = forgePlatformServiceModule.effectEmitter
        )
    }
    private val commandModule = CommandModule(
        coreModule = coreModule,
        soulsDaoModule = soulsDaoModule,
        plugin = plugin,
    )

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            coreModule.lifecycle,
            soulsDaoModule.lifecycle,
            forgeEventModule.lifecycle,
            serviceModule.lifecycle,
            commandModule.lifecycle
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
