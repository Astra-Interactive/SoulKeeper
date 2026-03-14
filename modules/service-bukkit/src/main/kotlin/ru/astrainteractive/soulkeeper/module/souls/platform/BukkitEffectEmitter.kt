package ru.astrainteractive.soulkeeper.module.souls.platform

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.asBukkitLocation
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.spawnParticleForPlayer

internal object BukkitEffectEmitter : EffectEmitter {
    override fun playSoundForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        location.asBukkitLocation()
            .playSoundForPlayer(bukkitPlayer, sound)
    }

    override fun spawnParticleForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        config: SoulsConfig.Particles.Particle
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        location.asBukkitLocation()
            .spawnParticleForPlayer(bukkitPlayer, config)
    }
}
