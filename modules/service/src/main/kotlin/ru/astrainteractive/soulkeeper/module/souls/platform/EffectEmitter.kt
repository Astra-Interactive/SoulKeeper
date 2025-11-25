package ru.astrainteractive.soulkeeper.module.souls.platform

import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig

interface EffectEmitter {

    fun playSoundForPlayer(
        location: Location,
        player: OnlineMinecraftPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    )

    fun spawnParticleForPlayer(
        location: Location,
        player: OnlineMinecraftPlayer,
        config: SoulsConfig.Particles.Particle
    )
}
