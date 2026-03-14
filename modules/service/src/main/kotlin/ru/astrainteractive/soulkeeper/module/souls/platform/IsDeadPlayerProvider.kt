package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer

interface IsDeadPlayerProvider {
    fun isDead(player: OnlineKPlayer): Boolean
}
