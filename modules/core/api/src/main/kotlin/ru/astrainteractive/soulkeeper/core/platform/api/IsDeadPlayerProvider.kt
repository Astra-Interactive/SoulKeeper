package ru.astrainteractive.soulkeeper.core.platform.api

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer

interface IsDeadPlayerProvider {
    fun isDead(player: OnlineKPlayer): Boolean
}
