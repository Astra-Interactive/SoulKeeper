package ru.astrainteractive.soulkeeper.module.souls.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.service.ServiceExecutor
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.server.IsDeadPlayerProvider

/**
 * This worker is required to pick up items
 */
internal class PickUpWorker(
    private val pickUpSoulUseCase: PickUpSoulUseCase,
    private val getNearestSoulUseCase: GetNearestSoulUseCase,
    private val soulsDao: SoulsDao,
    private val platformServer: PlatformServer,
    private val isDeadPlayerProvider: IsDeadPlayerProvider
) : ServiceExecutor, Logger by JUtiltLogger("AspeKt-PickUpWorker") {
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

    override suspend fun doWork() {
        if (mutex.isLocked) return
        mutex.withLock {
            processPickupSoulEvents()
        }
    }
}
