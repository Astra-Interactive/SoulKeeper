@file:Suppress("FunctionVerb")

package ru.astrainteractive.soulkeeper.service.api.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.service.ServiceTask
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.service.api.soul.usecase.FindNearestSoulUseCase
import ru.astrainteractive.soulkeeper.service.api.soul.usecase.PickUpSoulUseCase

internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val findNearestSoulUseCase: FindNearestSoulUseCase,
    private val soulRepository: SoulRepository,
    private val platformServer: PlatformServer,
    private val isDeadPlayerProvider: IsDeadPlayerProvider
) : ServiceTask, Logger by JUtiltLogger("SoulKeeper-PickUpWorker") {
    private val mutex = Mutex()

    private suspend fun processPickupSoulEvents() {
        platformServer.getOnlinePlayers()
            .filter { player -> !isDeadPlayerProvider.isDead(player) }
            .forEach { player ->
                val databaseSoul = findNearestSoulUseCase.invoke(player) ?: return@forEach
                val itemStackSoul = soulRepository.toItemDatabaseSoul(databaseSoul).getOrNull() ?: return@forEach
                pickUpSoulUseCase.invoke(player, itemStackSoul)
            }
    }

    private suspend fun doWorkInternal() {
        mutex.withLock { processPickupSoulEvents() }
    }

    override suspend fun execute() {
        supervisorScope { launch { doWorkInternal() } }
    }
}
