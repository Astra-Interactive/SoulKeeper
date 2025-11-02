package ru.astrainteractive.soulkeeper.command.di

import ru.astrainteractive.astralibs.command.api.registrar.PaperCommandRegistrarContext
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadCommandRegistrar
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListCommandRegistrar
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class CommandModule(
    private val coreModule: CoreModule,
    private val bukkitCoreModule: BukkitCoreModule,
    private val soulsDaoModule: SoulsDaoModule
) {
    private val paperCommandRegistrar = PaperCommandRegistrarContext(
        mainScope = coreModule.mainScope,
        plugin = bukkitCoreModule.plugin
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
                plugin = bukkitCoreModule.plugin,
                translationKrate = coreModule.translation,
                kyoriKrate = coreModule.kyoriComponentSerializer,
                registrarContext = paperCommandRegistrar
            )
        },
        onDisable = {
        }
    )
}
