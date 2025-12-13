package ru.astrainteractive.soulkeeper.command.souls

import net.minecraft.commands.CommandSourceStack

internal interface SoulsCommand {

    sealed interface Intent {
        data class List(val sender: CommandSourceStack, val page: Int) : Intent
        data class Free(val sender: CommandSourceStack, val soulId: Long) : Intent
        data class TeleportToSoul(val sender: CommandSourceStack, val soulId: Long) : Intent
    }

    companion object {
        internal const val PAGE_SIZE = 5
    }
}
