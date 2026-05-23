package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.MinecraftUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.soulkeeper.core.platform.Experienced

class ForgeExperienced(
    private val player: OnlineKPlayer
) : Experienced {
    override fun giveExperience(experience: Int) {
        MinecraftUtil.getOnlinePlayer(player.uuid)?.giveExperiencePoints(experience)
    }

    object OnlineMinecraftPlayerFactory : Experienced.Factory<OnlineKPlayer> {
        override fun create(owner: OnlineKPlayer): Experienced {
            return ForgeExperienced(owner)
        }
    }
}
