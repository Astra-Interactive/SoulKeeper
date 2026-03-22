package ru.astrainteractive.soulkeeper.command.di

import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.api.registrar.CommandRegistrarContext
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadCommandRegistrar
import ru.astrainteractive.soulkeeper.command.soulkrate.SoulKrateCommandRegistrar
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListCommandRegistrar
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class CommandModule(
    private val coreModule: CoreModule,
    private val soulsDaoModule: SoulsDaoModule,
    private val serviceModule: ServiceModule,
    private val commandRegistrarContext: CommandRegistrarContext,
    private val multiplatformCommand: MultiplatformCommand,
    private val lifecyclePlugin: Lifecycle
) {
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            SoulsListCommandRegistrar(
                kyoriKrate = coreModule.kyoriComponentSerializer,
                registrarContext = commandRegistrarContext,
                multiplatformCommand = multiplatformCommand,
                soulsCommandExecutor = SoulsCommandExecutor(
                    ioScope = coreModule.ioScope,
                    soulsDao = soulsDaoModule.soulsDao,
                    translationKrate = coreModule.translation,
                    kyoriKrate = coreModule.kyoriComponentSerializer,
                    dispatchers = coreModule.dispatchers
                ),
            ).register()
            SoulKrateCommandRegistrar(
                registrarContext = commandRegistrarContext,
                multiplatformCommand = multiplatformCommand,
                stringFormat = coreModule.yamlFormat,
                dataFolder = coreModule.dataFolder,
                ioScope = coreModule.ioScope,
                addSoulItemsIntoInventoryUseCase = serviceModule.addSoulItemsIntoInventoryUseCase,
                translationKrate = coreModule.translation,
                kyoriKrate = coreModule.kyoriComponentSerializer
            ).register()
            SoulsReloadCommandRegistrar(
                lifecyclePlugin = lifecyclePlugin,
                translationKrate = coreModule.translation,
                kyoriKrate = coreModule.kyoriComponentSerializer,
                registrarContext = commandRegistrarContext,
                multiplatformCommand = multiplatformCommand,
            ).register()
        },
        onDisable = {
        }
    )
}
