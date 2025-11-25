package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer

interface IsDeadPlayerProvider {
    fun isDead(player: OnlineMinecraftPlayer): Boolean
}
