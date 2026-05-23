package ru.astrainteractive.soulkeeper.core.platform

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer

interface IsDeadPlayerProvider {
    fun isDead(player: OnlineKPlayer): Boolean
}
