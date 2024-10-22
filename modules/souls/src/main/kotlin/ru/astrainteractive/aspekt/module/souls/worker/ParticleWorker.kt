package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.job.ScheduledJob
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.module.souls.util.playSound
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
internal class ParticleWorker(
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
    private val soulsConfigKrate: Krate<SoulsConfig>
) : ScheduledJob("ParticleWorker"), Logger by JUtiltLogger("AspeKt-ParticleWorker") {
    override val delayMillis: Long = 10.seconds.inWholeMilliseconds
    override val initialDelayMillis: Long = 0.milliseconds.inWholeMilliseconds
    override val isEnabled: Boolean = true
    private val scope = CoroutineFeature.Default(Dispatchers.IO)
    private val soulsConfig by soulsConfigKrate

    private val mutex = Mutex()
    private var lastJob: Job? = null

    private suspend fun playSounds(souls: List<Soul>) {
        withContext(dispatchers.Main) {
            souls.forEach { soul ->
                soulsConfig.sounds.calling.playSound(soul.location)
            }
        }
    }

    private suspend fun showParticles(souls: List<Soul>) = coroutineScope {
        val delay = 1000L
        souls.map { soul ->
            async {
                repeat(delayMillis.div(delay).toInt()) { i ->
                    delay(delay)

                    withContext(dispatchers.Main) {
                        if (soul.hasXp) {
                            soulsConfig.particles.soulXp.spawnParticle(soul.location)
                        }
                        if (soul.hasItems) {
                            soulsConfig.particles.soulItems.spawnParticle(soul.location)
                        }
                    }
                }
            }
        }.awaitAll()
    }

    override fun execute() {
        scope.launch {
            mutex.withLock {
                lastJob?.cancelAndJoin()
                lastJob = coroutineContext.job

                val souls = Bukkit.getOnlinePlayers()
                    .map(Player::getLocation)
                    .flatMap { soulsDao.getSoulsNear(it, soulsConfig.soulCallRadius).getOrNull().orEmpty() }
                playSounds(souls)
                showParticles(souls)
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        scope.coroutineContext.cancelChildren()
    }
}
