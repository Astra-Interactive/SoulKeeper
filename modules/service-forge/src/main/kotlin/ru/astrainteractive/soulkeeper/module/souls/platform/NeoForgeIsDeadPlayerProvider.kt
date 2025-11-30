package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.ForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer

object NeoForgeIsDeadPlayerProvider : IsDeadPlayerProvider {
    override fun isDead(player: OnlineMinecraftPlayer): Boolean {
        val serverPlayer = ForgeUtil.getOnlinePlayer(player.uuid) ?: return false
        return serverPlayer.isDeadOrDying
    }
}
