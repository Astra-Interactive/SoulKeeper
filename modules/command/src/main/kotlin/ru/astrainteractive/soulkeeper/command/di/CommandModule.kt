package ru.astrainteractive.soulkeeper.command.di

import ru.astrainteractive.astralibs.command.api.registrar.CommandRegistrarContext
import ru.astrainteractive.astralibs.command.api.registrar.registerWhenReady
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.error.CommandExceptionHandler
import ru.astrainteractive.soulkeeper.command.reload.command.SoulsReloadLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.command.soulkrate.command.SoulKrateLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.command.souls.command.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.command.SoulsListLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsModule

class CommandModule(
    coreModule: CoreModule,
    soulRepository: SoulRepository,
    soulsModule: SoulsModule,
    commandRegistrarContext: CommandRegistrarContext,
    lifecyclePlugin: Lifecycle
) {
    private val commandExceptionHandler = CommandExceptionHandler(
        multiplatformCommand = coreModule.multiplatformCommand,
        translationKrate = coreModule.translation
    )

    private val nodes = listOf(
        SoulsListLiteralArgumentBuilder(
            multiplatformCommand = coreModule.multiplatformCommand,
            commandExceptionHandler = commandExceptionHandler,
            soulsCommandExecutor = SoulsCommandExecutor(
                ioScope = coreModule.ioScope,
                soulRepository = soulRepository,
                translationKrate = coreModule.translation,
                dispatchers = coreModule.dispatchers,
            ),
        ).create(),
        SoulKrateLiteralArgumentBuilder(
            multiplatformCommand = coreModule.multiplatformCommand,
            stringFormat = coreModule.yamlFormat,
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope,
            playerInventory = soulsModule.playerInventory,
            translationKrate = coreModule.translation,
            commandExceptionHandler = commandExceptionHandler,
        ).create(),
        SoulsReloadLiteralArgumentBuilder(
            lifecyclePlugin = lifecyclePlugin,
            translationKrate = coreModule.translation,
            multiplatformCommand = coreModule.multiplatformCommand,
            commandExceptionHandler = commandExceptionHandler,
        ).create()
    )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            commandRegistrarContext.registerWhenReady(nodes, coreModule.unconfinedScope)
        }
    )
}
