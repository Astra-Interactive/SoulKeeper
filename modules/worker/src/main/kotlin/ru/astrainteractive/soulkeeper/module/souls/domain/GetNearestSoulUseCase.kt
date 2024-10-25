package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.database.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemStackSoul

/**
 * Get most near soul and available by current player
 */
internal class GetNearestSoulUseCase(
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-GetNearestSoulUseCase") {
    suspend fun invoke(player: Player): ItemStackSoul? {
        return soulsDao.getSoulsNear(player.location, 2)
            .getOrNull()
            .orEmpty()
            .firstOrNull { it.isFree || it.ownerUUID == player.uniqueId }
            ?.let { soul -> soulsDao.toItemStackSoul(soul) }
            ?.getOrNull()
    }
}
