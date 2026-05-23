package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.MinecraftUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider

object ForgeIsDeadPlayerProvider : IsDeadPlayerProvider {
    override fun isDead(player: OnlineKPlayer): Boolean {
        val serverPlayer = MinecraftUtil.getOnlinePlayer(player.uuid) ?: return true
        if (serverPlayer.health <= 1) return true
        if (!serverPlayer.isAlive) return true
        if (serverPlayer.isRemoved) return true
        if (serverPlayer.isDeadOrDying) return true
        return false
    }
}
