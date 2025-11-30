package ru.astrainteractive.soulkeeper.module.souls.platform

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import net.neoforged.neoforge.registries.NeoForgeRegistries
import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.ForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig

class NeoForgeEffectEmitter : EffectEmitter {
    override fun playSoundForPlayer(
        location: Location,
        player: OnlineMinecraftPlayer,
        sound: SoulsConfig.Sounds.SoundConfig
    ) {
        val serverPlayer = ForgeUtil.getOnlinePlayer(player.uuid) ?: return

        val soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(sound.id)) ?: return

        serverPlayer.playNotifySound(
            soundEvent,
            SoundSource.PLAYERS,
            sound.volume,
            sound.pitch
        )
    }

    override fun spawnParticleForPlayer(
        location: Location,
        player: OnlineMinecraftPlayer,
        config: SoulsConfig.Particles.Particle
    ) {
        val serverPlayer = ForgeUtil.getOnlinePlayer(player.uuid) ?: return
        val serverLevel = serverPlayer.serverLevel()

        NeoForgeRegistries.INGREDIENT_TYPES


        val particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(config.key)) ?: ParticleTypes.DUST

        val dustOptions = config.dustOptions
        val particleOptions = when (particleType) {
            ParticleTypes.DUST if dustOptions != null -> {
                val color = dustOptions.color
                val r = ((color shr 16) and 0xFF) / 255f
                val g = ((color shr 8) and 0xFF) / 255f
                val b = (color and 0xFF) / 255f

                net.minecraft.core.particles.DustParticleOptions(
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
