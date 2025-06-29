package ru.astrainteractive.soulkeeper.core.util

import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig

private fun SoulsConfig.Sounds.SoundConfig.toKyoriSound(): Sound {
    return Sound.sound(
        Key.key(this.id),
        Sound.Source.AMBIENT,
        volume,
        pitch,
    )
}

/**
 * Play sound only to this [Player]
 */
fun Location.playSoundForPlayer(player: Player, sound: SoulsConfig.Sounds.SoundConfig) {
    player.playSound(sound.toKyoriSound(), x, y, z)
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
 * Spawn particle to only this [player]
 */
fun Location.spawnParticleForPlayer(player: Player, config: SoulsConfig.Particles.Particle) {
    config
        .toBuilder(this)
        .receivers(player)
        .spawn()
}
