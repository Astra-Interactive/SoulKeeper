package ru.astrainteractive.soulkeeper.module.souls.service

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.service.ServiceTask
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase

/**
 * This worker is required to pick up items
 */
internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val getNearestSoulUseCase: GetNearestSoulUseCase,
    private val soulsDao: SoulsDao,
    private val platformServer: PlatformServer,
    private val isDeadPlayerProvider: IsDeadPlayerProvider
) : ServiceTask, Logger by JUtiltLogger("SoulKeeper-PickUpWorker") {
    private val mutex = Mutex()

    private suspend fun processPickupSoulEvents() {
        platformServer.getOnlinePlayers()
            .filter { !isDeadPlayerProvider.isDead(it) }
            .forEach { player ->
                val databaseSoul = getNearestSoulUseCase.invoke(player) ?: return@forEach
                val itemStackSoul = soulsDao.toItemDatabaseSoul(databaseSoul).getOrNull() ?: return@forEach
                when (pickUpSoulUseCase.invoke(player, itemStackSoul)) {
                    PickUpSoulUseCase.Output.AllPickedUp -> Unit

                    PickUpSoulUseCase.Output.SomethingRest -> Unit
                }
            }
    }

    private suspend fun doWorkInternal() {
        mutex.withLock { processPickupSoulEvents() }
    }

    override suspend fun execute() {
        supervisorScope { launch { doWorkInternal() } }
    }
}
