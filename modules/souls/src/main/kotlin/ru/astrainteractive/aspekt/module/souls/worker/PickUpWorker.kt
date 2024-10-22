package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.job.ScheduledJob
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.module.souls.util.spawnParticle
import ru.astrainteractive.aspekt.util.getValue
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to display soul particels near players
 */
internal class PickUpWorker(
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
    private val soulsConfigKrate: Krate<SoulsConfig>,
    private val onPickUp: () -> Unit
) : ScheduledJob("ParticleWorker"), Logger by JUtiltLogger("AspeKt-PickUpWorker") {
    override val delayMillis: Long = 3.seconds.inWholeMilliseconds
    override val initialDelayMillis: Long = 0.milliseconds.inWholeMilliseconds
    override val isEnabled: Boolean = true
    private val scope = CoroutineFeature.Default(Dispatchers.IO)
    private val soulsConfig by soulsConfigKrate

    private val mutex = Mutex()

    /**
     * Get most near soul and available by current player
     */
    private suspend fun getNearAvaliableSoul(player: Player): ItemStackSoul? {
        return soulsDao.getSoulsNear(player.location, 2)
            .getOrNull()
            .orEmpty()
            .also { info { "#processPickupSoulEvents ${it.size} souls near" } }
            .filter { it.isFree || it.ownerUUID == player.uniqueId }
            .firstOrNull()
            ?.let { soul -> soulsDao.toItemStackSoul(soul) }
            ?.getOrNull()
            .also { info { "#processPickupSoulEvents converted soul $it" } }
    }

    /**
     * @return true if all xp picked up, false if not
     */
    private suspend fun pickUpExp(player: Player, itemStackSoul: ItemStackSoul): Boolean {
        if (itemStackSoul.exp <= 0) {
            info { "#pickUpExp don't need pick up xp ${itemStackSoul.exp} ${itemStackSoul.soul.hasXp}" }
            return true
        }
        soulsConfig.sounds.collectXp.spawnParticle(itemStackSoul.soul.location)
        player.giveExp(itemStackSoul.exp)
        info { "#pickUpExp gave xp now updating" }
        soulsDao.updateSoul(
            itemStackSoul.copy(
                exp = 0,
                soul = itemStackSoul.soul.copy(
                    hasXp = false
                )
            )
        )
        return true
    }

    /**
     * @return true if all items picked up, false if not
     */
    private suspend fun pickUpItems(player: Player, itemStackSoul: ItemStackSoul): Boolean {
        if (itemStackSoul.items.isEmpty()) return true

        val notAddedItems = player.inventory.addItem(*itemStackSoul.items.toTypedArray()).values.toList()
        if (notAddedItems != itemStackSoul.items) {
            soulsConfig.sounds.collectItem.spawnParticle(itemStackSoul.soul.location)
        }
        soulsDao.updateSoul(
            itemStackSoul.copy(
                exp = 0,
                items = notAddedItems
            )
        )
        return notAddedItems.isEmpty()
    }

    private suspend fun pickUpSoul(player: Player, itemStackSoul: ItemStackSoul) {
        withContext(dispatchers.Main) {
            val isExpPickedUp = pickUpExp(player, itemStackSoul)
            val isAllItemsPickedUp = pickUpItems(player, itemStackSoul)

            if (!isExpPickedUp || !isAllItemsPickedUp) return@withContext
            soulsDao.deleteSoul(itemStackSoul.soul)
            soulsConfig.sounds.soulDisappear.spawnParticle(itemStackSoul.soul.location)
            soulsConfig.particles.soulGone.spawnParticle(itemStackSoul.soul.location)
            onPickUp.invoke()
        }
    }

    private suspend fun processPickupSoulEvents() {
        Bukkit.getOnlinePlayers()
            .filter { !it.isDead }
            .forEach { player ->
                info { "#processPickupSoulEvents ${player.name}" }
                val itemStackSoul = getNearAvaliableSoul(player) ?: return@forEach
                pickUpSoul(player, itemStackSoul)
            }
    }

    override fun execute() {
        if (mutex.isLocked) {
            info { "#execute last job still in progress" }
            return
        }
        scope.launch {
            mutex.withLock {
                processPickupSoulEvents()
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        scope.coroutineContext.cancelChildren()
    }
}
