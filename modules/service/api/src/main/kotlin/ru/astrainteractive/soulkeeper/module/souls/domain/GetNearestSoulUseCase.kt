package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

/**
 * Get most near soul and available by current player
 */
internal class GetNearestSoulUseCase(
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("SoulKeeper-GetNearestSoulUseCase") {
    suspend fun invoke(player: OnlineKPlayer): DatabaseSoul? {
        return soulsDao.getSoulsNear(player.getLocation(), 2)
            .getOrNull()
            .orEmpty()
            .firstOrNull { soul -> soul.isFree || soul.ownerUUID == player.uuid }
    }
}
