package ru.astrainteractive.soulkeeper.module.souls.service

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
import ru.astrainteractive.soulkeeper.module.souls.soul.usecase.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.soul.usecase.PickUpSoulUseCase

/**
 * This worker is required to pick up items
 */
internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val getNearestSoulUseCase: GetNearestSoulUseCase,
    private val soulRepository: SoulRepository,
    private val platformServer: PlatformServer,
    private val isDeadPlayerProvider: IsDeadPlayerProvider
) : ServiceTask, Logger by JUtiltLogger("SoulKeeper-PickUpWorker") {
    private val mutex = Mutex()

    private suspend fun processPickupSoulEvents() {
        platformServer.getOnlinePlayers()
            .filter { player -> !isDeadPlayerProvider.isDead(player) }
            .forEach { player ->
                val databaseSoul = getNearestSoulUseCase(player) ?: return@forEach
                val itemStackSoul = soulRepository.toItemDatabaseSoul(databaseSoul).getOrNull() ?: return@forEach
                pickUpSoulUseCase(player, itemStackSoul)
            }
    }

    private suspend fun doWorkInternal() {
        mutex.withLock { processPickupSoulEvents() }
    }

    override suspend fun execute() {
        supervisorScope { launch { doWorkInternal() } }
    }
}
