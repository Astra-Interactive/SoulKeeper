package ru.astrainteractive.soulkeeper.module.souls.worker.call

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.job.LifecycleCoroutineWorker
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to display soul particles near players
 */
internal class SoulCallWorker(
    private val soulCallRenderer: SoulCallRenderer,
) : LifecycleCoroutineWorker("ParticleWorker"), Logger by JUtiltLogger("AspeKt-ParticleWorker") {

    override fun getConfig(): Config = Config(
        delay = 10.seconds,
        initialDelay = 0.seconds
    )

    private val mutex = Mutex()
    private var lastJob: Job? = null

    override fun execute() {
        scope.launch {
            mutex.withLock {
                lastJob?.cancelAndJoin()
                lastJob = launch {
                    Bukkit.getOnlinePlayers()
                        .onEach { player -> soulCallRenderer.updateSoulsForPlayer(player) }
                        .onEach { player -> soulCallRenderer.displayArmorStands(player) }
                        .onEach { player -> soulCallRenderer.playSounds(player) }
                        .map { player -> launch { soulCallRenderer.displayParticlesContinuously(player) } }
                }
            }
        }
    }

    override fun onReload() {
        super.onReload()
        soulCallRenderer.clear()
    }

    override fun onDisable() {
        super.onDisable()
        soulCallRenderer.clear()
    }
}
