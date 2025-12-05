package ru.astrainteractive.soulkeeper.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.module.event.di.ForgeEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.NeoForgePlatformServiceModule
import java.io.File

class RootModule(private val plugin: Lifecycle) {

    private val dataFolder by lazy {
        FMLPaths.CONFIGDIR.get()
            .resolve("SoulKeeper")
            .toAbsolutePath()
            .toFile()
            .also(File::mkdirs)
    }



    private val forgePlatformServiceModule by lazy {
        NeoForgePlatformServiceModule(
        )
    }


    private val forgeEventModule by lazy {
        ForgeEventModule(
        )
    }
    private val commandModule = CommandModule(
    )

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            forgeEventModule.lifecycle,
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
