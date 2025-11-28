package ru.astrainteractive.soulkeeper.command.di

import ru.astrainteractive.astralibs.command.registrar.ForgeCommandRegistrarContext
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadCommandRegistrar
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListCommandRegistrar
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class CommandModule(
    private val coreModule: CoreModule,
    private val soulsDaoModule: SoulsDaoModule,
    private val plugin: Lifecycle
) {
    private val paperCommandRegistrar = ForgeCommandRegistrarContext(
        mainScope = coreModule.mainScope,
    )
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            SoulsListCommandRegistrar(
                kyoriKrate = coreModule.kyoriComponentSerializer,
                registrarContext = paperCommandRegistrar,
                soulsCommandExecutor = SoulsCommandExecutor(
                    ioScope = coreModule.ioScope,
                    soulsDao = soulsDaoModule.soulsDao,
                    translationKrate = coreModule.translation,
                    kyoriKrate = coreModule.kyoriComponentSerializer
                )
            ).register()
            SoulsReloadCommandRegistrar(
                plugin = plugin,
                translationKrate = coreModule.translation,
                kyoriKrate = coreModule.kyoriComponentSerializer,
                registrarContext = paperCommandRegistrar
            ).register()
        },
        onDisable = {
        }
    )
}
