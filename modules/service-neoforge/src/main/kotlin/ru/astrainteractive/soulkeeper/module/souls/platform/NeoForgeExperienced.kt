package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.NeoForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer

class NeoForgeExperienced(
    private val player: OnlineKPlayer
) : Experienced {
    override fun giveExperience(experience: Int) {
        NeoForgeUtil.getOnlinePlayer(player.uuid)?.giveExperiencePoints(experience)
    }

    object OnlineMinecraftPlayerFactory : Experienced.Factory<OnlineKPlayer> {
        override fun create(owner: OnlineKPlayer): Experienced {
            return NeoForgeExperienced(owner)
        }
    }
}
