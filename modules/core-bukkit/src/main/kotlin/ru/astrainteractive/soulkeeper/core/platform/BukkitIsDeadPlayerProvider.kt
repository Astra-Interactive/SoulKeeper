package ru.astrainteractive.soulkeeper.core.platform

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer

object BukkitIsDeadPlayerProvider : IsDeadPlayerProvider {
    override fun isDead(player: OnlineKPlayer): Boolean {
        return Bukkit.getPlayer(player.uuid)?.isDead == true
    }
}
