package ru.astrainteractive.soulkeeper.command.soul.api

import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.SoulId

internal interface SoulsCommand {

    sealed interface Intent {
        data class List(val sender: KCommandSender, val page: Int) : Intent
        data class Free(val sender: KCommandSender, val soulId: SoulId) : Intent
        data class TeleportToSoul(val player: OnlineKPlayer, val soulId: SoulId) : Intent
    }

    companion object {
        internal const val PAGE_SIZE = 5
    }
}
