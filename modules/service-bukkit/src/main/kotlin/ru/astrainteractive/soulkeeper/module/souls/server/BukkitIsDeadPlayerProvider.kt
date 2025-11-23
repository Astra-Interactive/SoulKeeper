package ru.astrainteractive.soulkeeper.module.souls.server

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer

internal object BukkitIsDeadPlayerProvider : IsDeadPlayerProvider {
    override fun isDead(player: OnlineMinecraftPlayer): Boolean {
        return Bukkit.getPlayer(player.uuid)?.isDead == true
    }
}
