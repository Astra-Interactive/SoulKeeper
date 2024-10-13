package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.job.ScheduledJob
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class ParticleWorker(
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers
) : ScheduledJob("ParticleWorker"), Logger by JUtiltLogger("ParticleWorker") {
    override val delayMillis: Long = 10.seconds.inWholeMilliseconds
    override val initialDelayMillis: Long = 0.milliseconds.inWholeMilliseconds
    override val isEnabled: Boolean = true

    private val scope = CoroutineFeature.Default(dispatchers.IO)

    private var lastJob: Job? = null
    private val mutex = Mutex()

    private suspend fun processPickupSoulEvents() {
        Bukkit.getOnlinePlayers()
            .forEach { player ->
                info { "#processPickupSoulEvents ${player.name}" }
                val itemStackSoul = soulsDao.getSoulsNear(player.location, 2)
                    .getOrNull()
                    .orEmpty()
                    .also { info { "#processPickupSoulEvents ${it.size} souls near" } }
                    .firstOrNull()
                    ?.let { soul -> soulsDao.toItemStackSoul(soul) }
                    ?.getOrNull()
                    .also { info { "#processPickupSoulEvents converted soul $it" } }
                    ?: return@forEach

                soulsDao.deleteSoul(itemStackSoul.soul)

                withContext(dispatchers.Main) {
                    player.inventory.addItem(*itemStackSoul.items.toTypedArray())
                        .values
                        .forEach { itemStack ->
                            player.location.world.dropItemNaturally(player.location, itemStack)
                        }
                }
            }
    }

    override fun execute() {
        scope.launch {
            mutex.withLock {
                lastJob?.cancelAndJoin()
                lastJob = coroutineContext.job

                processPickupSoulEvents()

                val souls = Bukkit.getOnlinePlayers()
                    .map(Player::getLocation)
                    .flatMap { soulsDao.getSoulsNear(it, 100).getOrNull().orEmpty() }
                souls.map { soul ->
                    async(dispatchers.Main) {
                        soul.location.world.playSound(
                            soul.location,
                            "block.beacon.ambient",
                            16f,
                            0.75f
                        )

                        repeat(delayMillis.div(1000L).toInt()) { i ->
                            delay(1000L)
                            soul.location.world.spawnParticle(
                                Particle.DUST,
                                soul.location,
                                30,
                                0.1,
                                0.1,
                                0.1,
                                Particle.DustOptions(Color.AQUA, 2f)
                            )
                        }
                    }
                }.awaitAll()
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        scope.cancel()
    }
}
