@file:Suppress("FunctionVerb")

package ru.astrainteractive.soulkeeper.module.souls.service

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

internal class DeleteSoulWorker(
    private val soulRepository: SoulRepository,
    configKrate: CachedKrate<SoulsConfig>
) : ServiceTask, Logger by JUtiltLogger("SoulKeeper-DeleteSoulWorker") {
    private val config by configKrate

    private suspend fun doWorkInternal() {
        val soulsToDelete = soulRepository.getSouls()
            .getOrNull()
            .orEmpty()
            .filter { soul ->
                val diff = Clock.System.now().minus(config.soulFadeAfter)
                soul.createdAt < diff
            }
        if (soulsToDelete.isEmpty()) return

        soulsToDelete.forEach { soul ->
            soulRepository.deleteSoul(soul.id)
        }
    }

    override suspend fun execute() {
        supervisorScope { launch { doWorkInternal() } }
    }
}
