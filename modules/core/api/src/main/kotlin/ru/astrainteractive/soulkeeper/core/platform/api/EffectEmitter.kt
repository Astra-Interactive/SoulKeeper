package ru.astrainteractive.soulkeeper.core.platform.api

import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig

interface EffectEmitter {

    fun playSoundForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    )

    fun spawnParticleForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        config: SoulsConfig.Particles.Particle
    )
}
