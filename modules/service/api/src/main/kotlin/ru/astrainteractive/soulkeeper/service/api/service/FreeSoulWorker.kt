@file:Suppress("FunctionVerb")

package ru.astrainteractive.soulkeeper.service.api.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.astrainteractive.astralibs.service.ServiceTask
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import kotlin.time.Clock

internal class FreeSoulWorker(
    private val soulRepository: SoulRepository,
    private val clock: Clock,
    configKrate: CachedKrate<SoulsConfig>
) : ServiceTask, Logger by JUtiltLogger("SoulKeeper-FreeSoulWorker") {
    private val config by configKrate

    private suspend fun doWorkInternal() {
        val soulsToFree = soulRepository.loadSouls()
            .getOrNull()
            .orEmpty()
            .filter { soul -> !soul.isFree }
            .filter { soul ->
                val diff = clock.now().minus(config.soulFreeAfter)
                soul.createdAt < diff
            }
        if (soulsToFree.isEmpty()) return

        info { "#execute found ${soulsToFree.size} souls to free" }
        soulsToFree
            .map { soul -> soul.copy(isFree = true) }
            .forEach { soul ->
                soulRepository.updateSoul(soul)
            }
    }

    override suspend fun execute() {
        supervisorScope { launch { doWorkInternal() } }
    }
}
