package ru.astrainteractive.soulkeeper.module.souls.platform

import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.MinecraftUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.soulkeeper.core.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig

object ForgeEffectEmitter : EffectEmitter {
    override fun playSoundForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    ) {
        val serverPlayer = MinecraftUtil.getOnlinePlayer(player.uuid) ?: return
        val soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(sound.id)) ?: return
        serverPlayer.playNotifySound(soundEvent, SoundSource.PLAYERS, sound.volume, sound.pitch)
    }

    @Suppress("MagicNumber")
    override fun spawnParticleForPlayer(
        location: KLocation,
        player: OnlineKPlayer,
        config: SoulsConfig.Particles.Particle
    ) {
        val serverPlayer = MinecraftUtil.getOnlinePlayer(player.uuid) ?: return
        val serverLevel = serverPlayer.serverLevel()
        val particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(config.key)) ?: ParticleTypes.DUST
        val dustOptions = config.dustOptions
        val particleOptions = when (particleType) {
            ParticleTypes.DUST if dustOptions != null -> {
                val color = dustOptions.color
                val r = ((color shr 16) and 0xFF) / 255f
                val g = ((color shr 8) and 0xFF) / 255f
                val b = (color and 0xFF) / 255f
                DustParticleOptions(
                    org.joml.Vector3f(r, g, b),
                    dustOptions.size
                )
            }
            else -> particleType as ParticleOptions
        }
        serverLevel.sendParticles(
            serverPlayer,
            particleOptions,
            false,
            location.x,
            location.y,
            location.z,
            config.count,
            0.0,
            0.0,
            0.0,
            0.0
        )
    }
}
