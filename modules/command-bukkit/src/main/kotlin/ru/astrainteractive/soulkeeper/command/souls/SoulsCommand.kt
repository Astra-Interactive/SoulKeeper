package ru.astrainteractive.soulkeeper.command.souls

import org.bukkit.command.CommandSender

internal interface SoulsCommand {

    sealed interface Intent {
        data class List(val sender: CommandSender, val page: Int) : Intent
        data class Free(val sender: CommandSender, val soulId: Long) : Intent
        data class TeleportToSoul(val sender: CommandSender, val soulId: Long) : Intent
    }

    companion object {
        internal const val PAGE_SIZE = 5
    }
}
