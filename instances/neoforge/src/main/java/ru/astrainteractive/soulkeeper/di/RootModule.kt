package ru.astrainteractive.soulkeeper.di

import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.soulkeeper.ForgeEntryPoint
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.module.event.di.ForgeEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.NeoForgePlatformServiceModule
import java.io.File

class RootModule(private val plugin: ForgeEntryPoint) {

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

}
