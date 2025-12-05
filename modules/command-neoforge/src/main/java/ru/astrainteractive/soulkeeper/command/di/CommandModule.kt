package ru.astrainteractive.soulkeeper.command.di

import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class CommandModule(
    private val coreModule: CoreModule,
    private val soulsDaoModule: SoulsDaoModule,
    private val plugin: Lifecycle,
) {
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            println(NeoForge.EVENT_BUS)
        },
        onDisable = {}
    )
}
