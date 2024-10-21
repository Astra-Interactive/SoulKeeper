package ru.astrainteractive.aspekt.module.souls.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.util.getValue
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.klibs.kstorage.api.Krate
import java.time.Instant

internal class SoulEvents(
    private val soulsDao: SoulsDao,
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
        val droppedXp = event.droppedExp.times(soulsConfig.retainedXp).toInt()
        event.droppedExp = 0
        if (soulItems.isEmpty() && droppedXp <= 0) return

        val itemStackSoul = ItemStackSoul(
            items = soulItems,
            exp = droppedXp,
            soul = Soul(
                ownerUUID = event.player.uniqueId,
                ownerName = event.player.name,
                createdAt = Instant.now(),
                isFree = false,
                location = event.player.location,
                hasItems = soulItems.isNotEmpty(),
                hasXp = droppedXp > 0
            )
        )
        itemStackSoul.soul.location.world.spawnParticle(
            Particle.DUST,
            itemStackSoul.soul.location,
            128,
            0.1,
            0.1,
            0.1,
            Particle.DustOptions(
                Color.fromRGB(soulsConfig.colors.soulCreated),
                64f
            )
        )
        itemStackSoul.soul.location.world.playSound(
            itemStackSoul.soul.location,
            soulsConfig.sounds.soulDropped,
            1f,
            0.75f
        )
        scope.launch { soulsDao.insertSoul(itemStackSoul) }
    }
}
