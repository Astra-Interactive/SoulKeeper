package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.util.toDatabaseLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

/**
 * Get most near soul and available by current player
 */
internal class GetNearestSoulUseCase(
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-GetNearestSoulUseCase") {
    suspend fun invoke(player: Player): DatabaseSoul? {
        return soulsDao.getSoulsNear(player.location.toDatabaseLocation(), 2)
            .getOrNull()
            .orEmpty()
            .firstOrNull { it.isFree || it.ownerUUID == player.uniqueId }
    }
}
