package ru.astrainteractive.soulkeeper.module.souls.platform

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.spawnParticleForPlayer
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation

internal object BukkitEffectEmitter : EffectEmitter {
    override fun playSoundForPlayer(
        location: Location,
        player: OnlineMinecraftPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        location.toBukkitLocation()
            .playSoundForPlayer(bukkitPlayer, sound)
    }

    override fun spawnParticleForPlayer(
        location: Location,
        player: OnlineMinecraftPlayer,
        config: SoulsConfig.Particles.Particle
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        location.toBukkitLocation()
            .spawnParticleForPlayer(bukkitPlayer, config)
    }
}
