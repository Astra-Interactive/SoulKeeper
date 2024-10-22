package ru.astrainteractive.aspekt.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger

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
            .also { info { "#processPickupSoulEvents ${it.size} souls near" } }
            .firstOrNull { it.isFree || it.ownerUUID == player.uniqueId }
            ?.let { soul -> soulsDao.toItemStackSoul(soul) }
            ?.getOrNull()
            .also { info { "#processPickupSoulEvents converted soul $it" } }
    }
}
