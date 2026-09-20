package ru.astrainteractive.soulkeeper.command.di

import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.api.registrar.CommandRegistrarContext
import ru.astrainteractive.astralibs.command.api.registrar.registerWhenReady
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.exception.CommandExceptionHandler
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.command.soulkrate.SoulKrateLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.command.souls.SoulsAccessPolicy
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class CommandModule(
    private val coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
    serviceModule: ServiceModule,
    private val commandRegistrarContext: CommandRegistrarContext,
    multiplatformCommand: MultiplatformCommand,
    lifecyclePlugin: Lifecycle
) {
    private val commandExceptionHandler = CommandExceptionHandler(
        multiplatformCommand = multiplatformCommand,
        translationKrate = coreModule.translation,
        kyoriKrate = coreModule.kyoriComponentSerializer
    )

    private val nodes = listOf(
        SoulsListLiteralArgumentBuilder(
            kyoriKrate = coreModule.kyoriComponentSerializer,
            multiplatformCommand = multiplatformCommand,
            commandExceptionHandler = commandExceptionHandler,
            soulsCommandExecutor = SoulsCommandExecutor(
                ioScope = coreModule.ioScope,
                soulsDao = soulsDaoModule.soulsDao,
                translationKrate = coreModule.translation,
                kyoriKrate = coreModule.kyoriComponentSerializer,
                dispatchers = coreModule.dispatchers,
                accessPolicy = SoulsAccessPolicy(),
            ),
        ).create(),
        SoulKrateLiteralArgumentBuilder(
            multiplatformCommand = multiplatformCommand,
            stringFormat = coreModule.yamlFormat,
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope,
            addSoulItemsIntoInventoryUseCase = serviceModule.addSoulItemsIntoInventoryUseCase,
            translationKrate = coreModule.translation,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            commandExceptionHandler = commandExceptionHandler,
        ).create(),
        SoulsReloadLiteralArgumentBuilder(
            lifecyclePlugin = lifecyclePlugin,
            translationKrate = coreModule.translation,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            multiplatformCommand = multiplatformCommand,
            commandExceptionHandler = commandExceptionHandler,
        ).create()
    )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            commandRegistrarContext.registerWhenReady(nodes, coreModule.unconfinedScope)
        }
    )
}
