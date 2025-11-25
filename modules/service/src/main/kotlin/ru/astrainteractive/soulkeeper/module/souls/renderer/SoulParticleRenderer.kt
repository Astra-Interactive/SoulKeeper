package ru.astrainteractive.soulkeeper.module.souls.renderer

import kotlinx.coroutines.withContext
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter

class SoulParticleRenderer(
    private val dispatchers: KotlinDispatchers,
    soulsConfigKrate: CachedKrate<SoulsConfig>,
    private val effectEmitter: EffectEmitter
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    override suspend fun renderOnce(player: OnlineMinecraftPlayer, souls: List<DatabaseSoul>) {
        withContext(dispatchers.Main) {
            souls.forEach { soul ->
                if (soul.exp > 0) {
                    effectEmitter.spawnParticleForPlayer(
                        location = soul.location,
                        player = player,
                        config = soulsConfig.particles.soulXp
                    )
                }
                if (soul.hasItems) {
                    effectEmitter.spawnParticleForPlayer(
                        location = soul.location,
                        player = player,
                        config = soulsConfig.particles.soulItems
                    )
                }
            }
        }
    }
}
