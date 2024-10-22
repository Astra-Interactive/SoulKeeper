package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import ru.astrainteractive.aspekt.job.ScheduledJob
import ru.astrainteractive.aspekt.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.aspekt.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to display soul particels near players
 */
internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val getNearestSoulUseCase: GetNearestSoulUseCase,
    private val onPickUp: () -> Unit
) : ScheduledJob("ParticleWorker"), Logger by JUtiltLogger("AspeKt-PickUpWorker") {
    override val delayMillis: Long = 3.seconds.inWholeMilliseconds
    override val initialDelayMillis: Long = 0.milliseconds.inWholeMilliseconds
    override val isEnabled: Boolean = true
    private val mutex = Mutex()
    private val scope = CoroutineFeature.Default(Dispatchers.IO)

    private suspend fun processPickupSoulEvents() {
        Bukkit.getOnlinePlayers()
            .filter { !it.isDead }
            .forEach { player ->
                val itemStackSoul = getNearestSoulUseCase.invoke(player) ?: return@forEach
                when (pickUpSoulUseCase.invoke(player, itemStackSoul)) {
                    PickUpSoulUseCase.Output.AllPickedUp -> onPickUp.invoke()
                    PickUpSoulUseCase.Output.SomethingRest -> Unit
                }
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
