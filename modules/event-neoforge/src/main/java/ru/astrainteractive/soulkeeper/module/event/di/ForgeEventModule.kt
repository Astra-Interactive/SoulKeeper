package ru.astrainteractive.soulkeeper.module.event.di

import net.neoforged.neoforge.common.NeoForge

class ForgeEventModule(
) {
    init {
        println(NeoForge.EVENT_BUS)
    }
}
