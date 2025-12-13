package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.NeoForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer

object NeoForgeIsDeadPlayerProvider : IsDeadPlayerProvider {
    override fun isDead(player: OnlineMinecraftPlayer): Boolean {
        val serverPlayer = NeoForgeUtil.getOnlinePlayer(player.uuid) ?: return true
        if (serverPlayer.health <= 1) return true
        if (!serverPlayer.isAlive) return true
        if (serverPlayer.isRemoved) return true
        if (serverPlayer.isDeadOrDying) return true
        return false
    }
}
