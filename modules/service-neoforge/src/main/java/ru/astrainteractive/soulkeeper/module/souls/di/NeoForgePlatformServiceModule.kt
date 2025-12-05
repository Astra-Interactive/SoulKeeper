package ru.astrainteractive.soulkeeper.module.souls.di

import net.neoforged.neoforge.common.NeoForge

class NeoForgePlatformServiceModule(
)  {

    init {
        println(NeoForge.EVENT_BUS)
    }
}
