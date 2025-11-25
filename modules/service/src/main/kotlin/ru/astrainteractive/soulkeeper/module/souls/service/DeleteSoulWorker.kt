package ru.astrainteractive.soulkeeper.module.souls.service

import ru.astrainteractive.astralibs.service.ServiceExecutor
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import java.time.Instant

internal class DeleteSoulWorker(
    private val soulsDao: SoulsDao,
    configKrate: CachedKrate<SoulsConfig>
) : ServiceExecutor, Logger by JUtiltLogger("SoulKeeper-DeleteSoulWorker") {
    private val config by configKrate

    override suspend fun doWork() {
        val soulsToDelete = soulsDao.getSouls()
            .getOrNull()
            .orEmpty()
            .filter { soul ->
                val diff = Instant.now().minusSeconds(config.soulFadeAfter.inWholeSeconds)
                soul.createdAt < diff
            }
        if (soulsToDelete.isEmpty()) return

        info { "#execute found ${soulsToDelete.size} souls to delete" }
        soulsToDelete.forEach { soul ->
            soulsDao.deleteSoul(soul.id)
        }
    }
}
