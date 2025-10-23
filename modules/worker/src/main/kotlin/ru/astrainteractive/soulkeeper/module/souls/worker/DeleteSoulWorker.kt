package ru.astrainteractive.soulkeeper.module.souls.worker

import kotlinx.coroutines.launch
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.job.LifecycleCoroutineWorker
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class DeleteSoulWorker(
    private val soulsDao: SoulsDao,
    configKrate: CachedKrate<SoulsConfig>
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
                soulsDao.deleteSoul(soul.id)
            }
        }
    }
}
