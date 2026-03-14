package ru.astrainteractive.soulkeeper.module.souls.platform

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer

class BukkitExperienced(
    private val player: OnlineKPlayer
) : Experienced {
    override fun giveExperience(experience: Int) {
        Bukkit.getPlayer(player.uuid)?.giveExp(experience)
    }

    object OnlineMinecraftPlayerFactory : Experienced.Factory<OnlineKPlayer> {
        override fun create(owner: OnlineKPlayer): Experienced {
            return BukkitExperienced(owner)
        }
    }
}
