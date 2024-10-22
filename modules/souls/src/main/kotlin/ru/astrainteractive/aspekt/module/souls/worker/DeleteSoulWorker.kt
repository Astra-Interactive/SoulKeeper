package ru.astrainteractive.aspekt.module.souls.worker

import kotlinx.coroutines.launch
import ru.astrainteractive.aspekt.job.LifecycleCoroutineWorker
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.util.getValue
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.kstorage.api.Krate
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class DeleteSoulWorker(
    private val soulsDao: SoulsDao,
    configKrate: Krate<SoulsConfig>
) : LifecycleCoroutineWorker("DeleteSoulWorker"), Logger by JUtiltLogger("AspeKt-DeleteSoulWorker") {
    private val config by configKrate

    override fun getConfig(): Config = Config(
        delay = 60.seconds,
        initialDelay = 0.seconds
    )

    override fun execute() {
        scope.launch {
            val soulsToFree = soulsDao.getSouls()
                .getOrNull()
                .orEmpty()
                .filter { soul ->
                    val diff = Instant.now().minusSeconds(config.soulFadeAfter.inWholeSeconds)
                    soul.createdAt < diff
                }
            info { "#execute found ${soulsToFree.size} souls to delete" }
            soulsToFree.forEach { soul ->
                soulsDao.deleteSoul(soul)
            }
        }
    }
}
