package ru.astrainteractive.soulkeeper.module.event.di

import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.astralibs.lifecycle.Lifecycle

class ForgeEventModule(
) {
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            println(NeoForge.EVENT_BUS)
        },
        onDisable = {
        }
    )
}
