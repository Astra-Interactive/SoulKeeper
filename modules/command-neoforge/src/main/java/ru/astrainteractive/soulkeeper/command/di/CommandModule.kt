package ru.astrainteractive.soulkeeper.command.di

import net.neoforged.neoforge.common.NeoForge

class CommandModule(
) {
    init {
        println(NeoForge.EVENT_BUS)
    }
}
