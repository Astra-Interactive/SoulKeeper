package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.ForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer

class NeoForgeExperienced(
    private val player: OnlineMinecraftPlayer
) : Experienced {
    override fun giveExperience(experience: Int) {
        ForgeUtil.getOnlinePlayer(player.uuid)?.giveExperiencePoints(experience)
    }

    object OnlineMinecraftPlayerFactory : Experienced.Factory<OnlineMinecraftPlayer> {
        override fun create(owner: OnlineMinecraftPlayer): Experienced {
            return NeoForgeExperienced(owner)
        }
    }
}
