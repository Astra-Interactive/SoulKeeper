package ru.astrainteractive.soulkeeper.core.platform.fake

import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.core.platform.api.EffectEmitter
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig

class FakeEffectEmitter : EffectEmitter {
    val playedSounds = mutableListOf<SoulsConfig.Sounds.SoundConfig>()
    val spawnedParticles = mutableListOf<SoulsConfig.Particles.Particle>()

    override fun playSoundForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    ) {
        playedSounds += sound
    }

    override fun spawnParticleForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        config: SoulsConfig.Particles.Particle
    ) {
        spawnedParticles += config
    }
}
