package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul

/**
 * Get most near soul and available by current player
 */
internal class GetNearestSoulUseCase(
    private val soulRepository: SoulRepository,
) : Logger by JUtiltLogger("SoulKeeper-GetNearestSoulUseCase") {
    suspend fun invoke(player: OnlineKPlayer): DatabaseSoul? {
        return soulRepository.getSoulsNear(player.getLocation(), 2)
            .getOrNull()
            .orEmpty()
            .firstOrNull { soul -> soul.isFree || soul.ownerUUID == player.uuid }
    }
}
