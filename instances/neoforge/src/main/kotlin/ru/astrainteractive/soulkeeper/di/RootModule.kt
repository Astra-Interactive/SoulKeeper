package ru.astrainteractive.soulkeeper.di

import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.brigadier.command.MinecraftMultiplatformCommands
import ru.astrainteractive.astralibs.command.registrar.NeoForgeCommandRegistrarContext
import ru.astrainteractive.astralibs.coroutines.MinecraftDispatchers
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.NeoForgeEffectEmitter
import ru.astrainteractive.soulkeeper.data.exposed.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.event.di.ForgeEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.NeoForgePlatformSoulsModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsModule
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
            dispatchers = MinecraftDispatchers(),
            effectEmitter = NeoForgeEffectEmitter,
            multiplatformCommand = MultiplatformCommand(MinecraftMultiplatformCommands())
        )
    }

    private val soulsDaoModule by lazy {
        SoulsDaoModule.Default(
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope,
            dispatchers = coreModule.dispatchers
        )
    }
    private val forgePlatformSoulsModule by lazy {
        NeoForgePlatformSoulsModule()
    }

    private val soulsModule by lazy {
        SoulsModule(
            coreModule = coreModule,
            soulRepository = soulsDaoModule.soulRepository,
            platformSoulsModule = forgePlatformSoulsModule
        )
    }
    private val forgeEventModule by lazy {
        ForgeEventModule(
            coreModule = coreModule,
            soulRepository = soulsDaoModule.soulRepository,
            effectEmitter = coreModule.effectEmitter
        )
    }
    private val commandRegistrarContext = NeoForgeCommandRegistrarContext(mainScope = coreModule.unconfinedScope)
    private val commandModule by lazy {
        CommandModule(
            coreModule = coreModule,
            soulRepository = soulsDaoModule.soulRepository,
            commandRegistrarContext = commandRegistrarContext,
            soulsModule = soulsModule,
            lifecyclePlugin = plugin,
        )
    }

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            coreModule.lifecycle,
            soulsDaoModule.lifecycle,
            forgeEventModule.lifecycle,
            soulsModule.lifecycle,
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
