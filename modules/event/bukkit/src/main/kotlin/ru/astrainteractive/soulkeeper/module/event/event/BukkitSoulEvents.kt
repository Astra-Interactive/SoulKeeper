package ru.astrainteractive.soulkeeper.module.event.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.asKLocation
import ru.astrainteractive.astralibs.server.util.asOnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.serialization.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.krate.PlayerSoulKrate
import java.io.File
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class BukkitSoulEvents(
    private val soulsDao: SoulsDao,
    private val ioScope: CoroutineScope,
    private val dataFolder: File,
    private val stringFormat: StringFormat,
    soulsConfigKrate: CachedKrate<SoulsConfig>,
    private val effectEmitter: EffectEmitter,
) : EventListener, Logger by JUtiltLogger("SoulKeeper-BukkitSoulEvents") {
    private val soulsConfig by soulsConfigKrate

    private fun getAndClearItems(event: PlayerDeathEvent): List<StringFormatObject> {
        return when {
            event.keepInventory -> emptyList()
            else -> {
                event.drops
                    .map(ItemStackSerializer::encodeToString)
                    .map(::StringFormatObject)
                    .also { event.drops.clear() }
            }
        }
    }

    private fun getSoulLocation(event: PlayerDeathEvent): KLocation {
        return when {
            event.player.location.world.environment == World.Environment.THE_END -> {
                val endLocation = event.player.location.clone()
                if (endLocation.y < soulsConfig.endLocationLimitY) {
                    endLocation.y = soulsConfig.endLocationLimitY
                }
                endLocation
            }

            else -> event.player.location
        }.asKLocation()
    }

    private fun getAndClearDroppedXp(event: PlayerDeathEvent): Int {
        return when {
            event.keepLevel -> 0

            else -> {
                val exp = event.droppedExp.times(soulsConfig.retainedXp).toInt()
                event.droppedExp = 0
                exp
            }
        }
    }

    private fun playEffects(soul: DefaultSoul, player: OnlineKPlayer) {
        effectEmitter.spawnParticleForPlayer(
            location = soul.location,
            player = player,
            config = soulsConfig.particles.soulCreated
        )
        effectEmitter.playSoundForPlayer(
            location = soul.location,
            player = player,
            sound = soulsConfig.sounds.soulDropped
        )
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun playerDeathEvent(event: PlayerDeathEvent) {
        val soulItems = getAndClearItems(event)
        val soulExp = getAndClearDroppedXp(event)

        if (soulItems.isEmpty() && soulExp <= 0) return

        val soul = DefaultSoul(
            exp = soulExp,
            ownerUUID = event.player.uniqueId,
            ownerLastName = event.player.name,
            createdAt = Instant.now(),
            isFree = soulsConfig.soulFreeAfter == 0.seconds,
            location = getSoulLocation(event),
            items = soulItems,
        )
        ioScope.launch {
            PlayerSoulKrate(
                stringFormat = stringFormat,
                dataFolder = dataFolder,
                createdAt = soul.createdAt,
                ownerUUID = soul.ownerUUID,
            ).save(soul)
        }
        ioScope.launch { soulsDao.insertSoul(soul) }
        info { "#playerDeathEvent soul: $soul" }
        playEffects(soul, event.player.asOnlineMinecraftPlayer())
    }
}
