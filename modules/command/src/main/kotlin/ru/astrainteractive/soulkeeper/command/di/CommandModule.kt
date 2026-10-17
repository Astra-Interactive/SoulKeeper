package ru.astrainteractive.soulkeeper.command.di

import ru.astrainteractive.astralibs.command.api.registrar.CommandRegistrarContext
import ru.astrainteractive.astralibs.command.api.registrar.registerWhenReady
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.command.soulkrate.SoulKrateLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.command.souls.SoulsAccessPolicy
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListLiteralArgumentBuilder
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule

class CommandModule(
    private val coreModule: CoreModule,
    soulRepository: SoulRepository,
    serviceModule: ServiceModule,
    private val commandRegistrarContext: CommandRegistrarContext,
    lifecyclePlugin: Lifecycle
) {
    private val nodes = listOf(
        SoulsListLiteralArgumentBuilder(
            multiplatformCommand = coreModule.multiplatformCommand,
            commandExceptionHandler = coreModule.commandExceptionHandler,
            soulsCommandExecutor = SoulsCommandExecutor(
                ioScope = coreModule.ioScope,
                soulRepository = soulRepository,
                translationKrate = coreModule.translation,
                dispatchers = coreModule.dispatchers,
                accessPolicy = SoulsAccessPolicy(),
            ),
        ).create(),
        SoulKrateLiteralArgumentBuilder(
            multiplatformCommand = coreModule.multiplatformCommand,
            stringFormat = coreModule.yamlFormat,
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope,
            playerInventory = serviceModule.playerInventory,
            translationKrate = coreModule.translation,
            commandExceptionHandler = coreModule.commandExceptionHandler,
        ).create(),
        SoulsReloadLiteralArgumentBuilder(
            lifecyclePlugin = lifecyclePlugin,
            translationKrate = coreModule.translation,
            multiplatformCommand = coreModule.multiplatformCommand,
            commandExceptionHandler = coreModule.commandExceptionHandler,
        ).create()
    )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            commandRegistrarContext.registerWhenReady(nodes, coreModule.unconfinedScope)
        }
    )
}
