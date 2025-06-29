package ru.astrainteractive.soulkeeper.module.souls.renderer

import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.spawnParticleForPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer
import ru.astrainteractive.soulkeeper.module.souls.util.toBukkitLocation

class SoulParticleRenderer(
    private val dispatchers: KotlinDispatchers,
    soulsConfigKrate: Krate<SoulsConfig>
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    override suspend fun renderOnce(player: Player, souls: List<DatabaseSoul>) {
        withContext(dispatchers.Main) {
            souls.forEach { soul ->
                if (soul.exp>0) {
                    soul.location.toBukkitLocation().spawnParticleForPlayer(
                        player = player,
                        config = soulsConfig.particles.soulXp
                    )
                }
                if (soul.hasItems) {
                    soul.location.toBukkitLocation().spawnParticleForPlayer(
                        player = player,
                        config = soulsConfig.particles.soulItems
                    )
                }
            }
        }
    }
}
