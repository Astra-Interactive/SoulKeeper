package ru.astrainteractive.soulkeeper.module.souls.worker

import kotlinx.coroutines.launch
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.job.LifecycleCoroutineWorker
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

internal class FreeSoulWorker(
    private val soulsDao: SoulsDao,
    configKrate: CachedKrate<SoulsConfig>
) : LifecycleCoroutineWorker("FreeSoulWorker"), Logger by JUtiltLogger("AspeKt-FreeSoulWorker") {
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
                .filter { soul -> !soul.isFree }
                .filter { soul ->
                    val diff = Instant.now().minusSeconds(config.soulFreeAfter.inWholeSeconds)
                    soul.createdAt < diff
                }
            if (soulsToFree.isEmpty()) return@launch

            info { "#execute found ${soulsToFree.size} souls to free" }
            soulsToFree
                .map { soul -> soul.copy(isFree = true) }
                .forEach { soul ->
                    soulsDao.updateSoul(soul)
                }
        }
    }
}
