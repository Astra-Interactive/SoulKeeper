package ru.astrainteractive.soulkeeper.module.event.event

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import ru.astrainteractive.astralibs.async.withTimings
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.coroutines.CoroutineFeature
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.serialization.ItemStackSerializer
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.spawnParticleForPlayer
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.core.util.toDatabaseLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class BukkitSoulEvents(
    private val soulsDao: SoulsDao,
    soulsConfigKrate: CachedKrate<SoulsConfig>
) : EventListener {
    private val scope = CoroutineFeature.IO.withTimings()
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

        val bukkitSoul = DefaultSoul(
            exp = droppedXp,
            ownerUUID = event.player.uniqueId,
            ownerLastName = event.player.name,
            createdAt = Instant.now(),
            isFree = soulsConfig.soulFreeAfter == 0.seconds,
            location = when {
                event.player.location.world.environment == World.Environment.THE_END -> {
                    val endLocation = event.player.location.clone()
                    if (endLocation.y < soulsConfig.endLocationLimitY) {
                        endLocation.y = soulsConfig.endLocationLimitY
                    }
                    endLocation
                }

                else -> event.player.location
            }.toDatabaseLocation(),
            hasItems = soulItems.isNotEmpty(),
            items = soulItems
                .map(ItemStackSerializer::encodeToString)
                .map(::StringFormatObject),
        )
        bukkitSoul.location.toBukkitLocation().spawnParticleForPlayer(
            event.player,
            soulsConfig.particles.soulCreated,
        )
        bukkitSoul.location.toBukkitLocation().playSoundForPlayer(event.player, soulsConfig.sounds.soulDropped)
        scope.launch {
            soulsDao.insertSoul(bukkitSoul)
        }
    }

    override fun onDisable() {
        scope.cancel()
        super.onDisable()
    }
}
