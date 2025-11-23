package ru.astrainteractive.soulkeeper.module.souls.server

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer

object IsDeadPlayerProviderBukkit : IsDeadPlayerProvider {
    override fun isDead(player: OnlineMinecraftPlayer): Boolean {
        return Bukkit.getPlayer(player.uuid)?.isDead == true
    }
}
