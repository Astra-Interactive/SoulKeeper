package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import ru.astrainteractive.aspekt.job.ScheduledJob
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
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

    override fun execute() {
        scope.launch {
            mutex.withLock {
                lastJob?.cancelAndJoin()
                lastJob = coroutineContext.job
                Bukkit.getOnlinePlayers()
                    .onEach { player ->
                        soulsDao.getSoulsNear(location = player.location, radius = soulsConfig.soulCallRadius)
                            .getOrNull()
                            .orEmpty()
                            .filter { it.isFree || it.ownerUUID == player.uniqueId }
                            .onEach { soul ->
                                withContext(dispatchers.Main) {
                                    player.playSound(soul.location, soulsConfig.sounds.calling)
                                }
                                repeat(delayMillis.div(1000L).toInt()) {
                                    delay(1000L)
                                    if (soul.hasXp) {
                                        player.spawnParticle(soul.location, soulsConfig.particles.soulXp)
                                    }
                                    if (soul.hasItems) {
                                        player.spawnParticle(soul.location, soulsConfig.particles.soulItems)
                                    }
                                }
                            }
                    }
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        scope.coroutineContext.cancelChildren()
    }
}
