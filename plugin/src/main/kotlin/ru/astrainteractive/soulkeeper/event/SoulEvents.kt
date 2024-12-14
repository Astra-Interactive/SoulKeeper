package ru.astrainteractive.soulkeeper.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSound
import ru.astrainteractive.soulkeeper.core.util.spawnParticle
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.worker.call.SoulCallRenderer
import java.time.Instant

internal class SoulEvents(
    private val soulsDao: SoulsDao,
    private val soulCallRenderer: SoulCallRenderer,
    soulsConfigKrate: Krate<SoulsConfig>
) : EventListener {
    private val scope = CoroutineFeature.Default(Dispatchers.IO)
    private val soulsConfig by soulsConfigKrate

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun playerDeathEvent(event: PlayerDeathEvent) {
        val soulItems = when {
            event.keepInventory -> emptyList()

            else -> {
                val drops = event.drops.toList()
                event.drops.clear()
                drops
            }
        }

        val droppedXp = when {
            event.keepLevel -> 0

            else -> {
                val exp = event.droppedExp.times(soulsConfig.retainedXp).toInt()
                event.droppedExp = 0
                exp
            }
        }

        if (soulItems.isEmpty() && droppedXp <= 0) return

        val bukkitSoul = BukkitSoul(
            items = soulItems,
            exp = droppedXp,
            ownerUUID = event.player.uniqueId,
            ownerLastName = event.player.name,
            createdAt = Instant.now(),
            isFree = false,
            location = when {
                event.player.location.world.environment == World.Environment.THE_END -> {
                    val endLocation = event.player.location.clone()
                    if (endLocation.y < 0) {
                        endLocation.y = 0.0
                    }
                    endLocation
                }

                else -> event.player.location
            },
        )
        bukkitSoul.location.spawnParticle(soulsConfig.particles.soulCreated, soulsConfig.soulCallRadius)
        bukkitSoul.location.playSound(soulsConfig.sounds.soulDropped)
        scope.launch {
            soulsDao.insertSoul(bukkitSoul)
                .onSuccess { soul -> soulCallRenderer.rememberSoul(soul) }
        }
    }

    override fun onDisable() {
        scope.cancel()
        super.onDisable()
    }
}
