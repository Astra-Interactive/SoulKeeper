package ru.astrainteractive.soulkeeper.command.souls

import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender

internal interface SoulsCommand {

    sealed interface Intent {
        data class List(val sender: KCommandSender, val page: Int) : Intent
        data class Free(val sender: KCommandSender, val soulId: Long) : Intent
        data class TeleportToSoul(val sender: KCommandSender, val soulId: Long) : Intent
    }

    companion object {
        internal const val PAGE_SIZE = 5
    }
}
