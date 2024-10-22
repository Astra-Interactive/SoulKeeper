package ru.astrainteractive.aspekt.module.souls.util

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig

internal fun SoulsConfig.Sounds.SoundConfig.spawnParticle(location: Location) {
    location.world.playSound(
        location,
        this.id,
        this.volume,
        this.pitch
    )
}

internal fun SoulsConfig.Particles.Particle.spawnParticle(location: Location) {
    val particle = runCatching { Particle.valueOf(key) }
        .getOrNull()
        ?: Particle.DUST
    val localDustOptions = dustOptions
        ?.takeIf { particle == Particle.DUST }

    location.world.spawnParticle(
        particle,
        location,
        count,
        0.1,
        0.1,
        0.1,
        localDustOptions?.let {
            Particle.DustOptions(
                Color.fromRGB(localDustOptions.color),
                localDustOptions.size
            )
        }
    )
}
