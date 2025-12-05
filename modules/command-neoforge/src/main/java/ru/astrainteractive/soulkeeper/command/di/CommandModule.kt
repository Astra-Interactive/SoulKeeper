package ru.astrainteractive.soulkeeper.command.di

import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.astralibs.lifecycle.Lifecycle

class CommandModule(
) {
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            println(NeoForge.EVENT_BUS)
        },
        onDisable = {}
    )
}
