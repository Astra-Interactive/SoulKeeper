package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import ru.astrainteractive.aspekt.job.LifecycleCoroutineWorker
import ru.astrainteractive.aspekt.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.aspekt.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to pick up items
 */
internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val getNearestSoulUseCase: GetNearestSoulUseCase,
    private val onPickUp: () -> Unit
) : LifecycleCoroutineWorker("ParticleWorker"), Logger by JUtiltLogger("AspeKt-PickUpWorker") {

    override fun getConfig(): Config = Config(
        delay = 3.seconds,
        initialDelay = 0.seconds
    )

    private val mutex = Mutex()

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
}
