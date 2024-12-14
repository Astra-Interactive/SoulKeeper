package ru.astrainteractive.soulkeeper.module.souls.worker

import kotlinx.coroutines.launch
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.job.LifecycleCoroutineWorker
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.worker.call.SoulCallRenderer
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class DeleteSoulWorker(
    private val soulsDao: SoulsDao,
    private val soulCallRenderer: SoulCallRenderer,
    configKrate: Krate<SoulsConfig>
) : LifecycleCoroutineWorker("DeleteSoulWorker"), Logger by JUtiltLogger("AspeKt-DeleteSoulWorker") {
    private val config by configKrate

    override fun getConfig(): Config = Config(
        delay = 60.seconds,
        initialDelay = 0.seconds
    )

    override fun execute() {
        scope.launch {
            val soulsToDelete = soulsDao.getSouls()
                .getOrNull()
                .orEmpty()
                .filter { soul ->
                    val diff = Instant.now().minusSeconds(config.soulFadeAfter.inWholeSeconds)
                    soul.createdAt < diff
                }
            if (soulsToDelete.isEmpty()) return@launch

            info { "#execute found ${soulsToDelete.size} souls to delete" }
            soulsToDelete.forEach { soul ->
                soulsDao.deleteSoul(soul)
                soulCallRenderer.removeSoul(soul)
            }
        }
    }
}
