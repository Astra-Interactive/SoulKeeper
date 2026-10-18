package ru.astrainteractive.soulkeeper.module.souls.service

import kotlinx.coroutines.withContext
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.platform.api.EffectEmitter
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer

internal class SoulParticleRenderer(
    private val dispatchers: KotlinDispatchers,
    soulsConfigKrate: CachedKrate<SoulsConfig>,
    private val effectEmitter: EffectEmitter
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    override suspend fun renderOnce(player: OnlineKPlayer, souls: List<DatabaseSoul>) {
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
