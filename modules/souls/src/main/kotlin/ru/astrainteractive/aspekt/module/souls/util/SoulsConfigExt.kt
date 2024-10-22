package ru.astrainteractive.aspekt.module.souls.util

import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig

private fun SoulsConfig.Sounds.SoundConfig.toKyoriSound(): net.kyori.adventure.sound.Sound {
    return net.kyori.adventure.sound.Sound.sound(
        Key.key(this.id),
        net.kyori.adventure.sound.Sound.Source.AMBIENT,
        volume,
        pitch
    )
}

/**
 * Play sound in this [Location] for all players
 */
internal fun Location.playSound(sound: SoulsConfig.Sounds.SoundConfig) {
    this.world.playSound(sound.toKyoriSound())
}

/**
 * Play sound only to this [Player]
 */
internal fun Player.playSound(location: Location, sound: SoulsConfig.Sounds.SoundConfig) {
    playSound(sound.toKyoriSound(), location.x, location.y, location.z)
}

private fun SoulsConfig.Particles.Particle.toBuilder(location: Location): ParticleBuilder {
    val particle = runCatching { Particle.valueOf(key) }
        .getOrNull()
        ?: Particle.DUST
    val localDustOptions = dustOptions
        ?.takeIf { particle == Particle.DUST }

    return ParticleBuilder(particle)
        .count(count)
        .location(location)
        .offset(0.1, 0.1, 0.1)
        .apply {
            if (localDustOptions != null) {
                data(
                    Particle.DustOptions(
                        Color.fromRGB(localDustOptions.color),
                        localDustOptions.size
                    )
                )
            }
        }
}

/**
 * Spawn particle to only this [Player]
 */
internal fun Player.spawnParticle(location: Location, config: SoulsConfig.Particles.Particle) {
    config
        .toBuilder(location)
        .receivers(this)
        .spawn()
}

/**
 * Spawn particle to all player in radius [radius]
 */
internal fun Location.spawnParticle(config: SoulsConfig.Particles.Particle, radius: Int = 64) {
    config
        .toBuilder(this)
        .receivers(radius)
        .spawn()
}
