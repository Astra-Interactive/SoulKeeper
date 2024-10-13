package ru.astrainteractive.aspekt.module.souls.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.event.EventListener
import java.time.Instant

internal class SoulEvents(
    private val soulsDao: SoulsDao
) : EventListener {
    private val scope = CoroutineFeature.Default(Dispatchers.IO)

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

        val itemStackSoul = ItemStackSoul(
            items = soulItems,
            soul = Soul(
                ownerUUID = event.player.uniqueId,
                ownerName = event.player.name,
                createdAt = Instant.now(),
                isFree = false,
                location = event.player.location
            )
        )
        scope.launch { soulsDao.insertSoul(itemStackSoul) }
    }
}
