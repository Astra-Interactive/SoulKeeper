package ru.astrainteractive.soulkeeper.core.platform

import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.asBukkitLocation
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig

object BukkitEffectEmitter : EffectEmitter {
    private fun SoulsConfig.Sounds.SoundConfig.toKyoriSound(): Sound {
        return Sound.sound(
            Key.key(this.id),
            Sound.Source.AMBIENT,
            volume,
            pitch,
        )
    }

    override fun playSoundForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        val location = location.asBukkitLocation()
        bukkitPlayer.playSound(sound.toKyoriSound(), location.x, location.y, location.z)
    }

    @Suppress("MagicNumber")
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

    override fun spawnParticleForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        config: SoulsConfig.Particles.Particle
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return

        config
            .toBuilder(location.asBukkitLocation())
            .receivers(bukkitPlayer)
            .spawn()
    }
}
