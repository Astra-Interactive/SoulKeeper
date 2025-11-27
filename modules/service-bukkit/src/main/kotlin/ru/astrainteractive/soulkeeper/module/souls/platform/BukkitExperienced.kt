package ru.astrainteractive.soulkeeper.module.souls.platform

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer

class BukkitExperienced(
    private val player: OnlineMinecraftPlayer
) : Experienced {
    override fun giveExperience(experience: Int) {
        Bukkit.getPlayer(player.uuid)?.giveExp(experience)
    }

    object OnlineMinecraftPlayerFactory : Experienced.Factory<OnlineMinecraftPlayer> {
        override fun create(owner: OnlineMinecraftPlayer): Experienced {
            return BukkitExperienced(owner)
        }
    }
}
