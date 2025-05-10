package ru.astrainteractive.soulkeeper.module.souls.renderer

import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.renderer.api.SoulEffectRenderer

class SoulSoundRenderer(
    private val dispatchers: KotlinDispatchers,
    soulsConfigKrate: Krate<SoulsConfig>
) : SoulEffectRenderer {
    private val soulsConfig by soulsConfigKrate

    override suspend fun renderOnce(player: Player, souls: List<DatabaseSoul>) {
        withContext(dispatchers.Main) {
            souls.forEach { soul ->
                soul.location.playSoundForPlayer(player, soulsConfig.sounds.calling)
            }
        }
    }
}
