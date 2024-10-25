package ru.astrainteractive.soulkeeper.module.souls.worker

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.job.LifecycleCoroutineWorker
import ru.astrainteractive.soulkeeper.module.souls.database.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.worker.call.SoulCallRenderer
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to pick up items
 */
internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val soulCallRenderer: SoulCallRenderer,
    private val getNearestSoulUseCase: GetNearestSoulUseCase,
    private val soulsDao: SoulsDao,
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
                val databaseSoul = getNearestSoulUseCase.invoke(player) ?: return@forEach
                val itemStackSoul = soulsDao.toItemStackSoul(databaseSoul).getOrNull() ?: return@forEach
                when (pickUpSoulUseCase.invoke(player, itemStackSoul)) {
                    PickUpSoulUseCase.Output.AllPickedUp -> {
                        soulCallRenderer.removeSoul(databaseSoul)
                    }

                    PickUpSoulUseCase.Output.SomethingRest -> Unit
                }
            }
    }

    override fun execute() {
        if (mutex.isLocked) {
            return
        }
        scope.launch {
            mutex.withLock {
                processPickupSoulEvents()
            }
        }
    }
}
