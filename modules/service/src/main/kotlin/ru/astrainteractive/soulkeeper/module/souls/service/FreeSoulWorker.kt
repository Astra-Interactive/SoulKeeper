package ru.astrainteractive.soulkeeper.module.souls.service

import ru.astrainteractive.astralibs.service.ServiceExecutor
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import java.time.Instant

internal class FreeSoulWorker(
    private val soulsDao: SoulsDao,
    configKrate: CachedKrate<SoulsConfig>
) : ServiceExecutor, Logger by JUtiltLogger("SoulKeeper-FreeSoulWorker") {
    private val config by configKrate

    override suspend fun doWork() {
        val soulsToFree = soulsDao.getSouls()
            .getOrNull()
            .orEmpty()
            .filter { soul -> !soul.isFree }
            .filter { soul ->
                val diff = Instant.now().minusSeconds(config.soulFreeAfter.inWholeSeconds)
                soul.createdAt < diff
            }
        if (soulsToFree.isEmpty()) return

        info { "#execute found ${soulsToFree.size} souls to free" }
        soulsToFree
            .map { soul -> soul.copy(isFree = true) }
            .forEach { soul ->
                soulsDao.updateSoul(soul)
            }
    }
}
