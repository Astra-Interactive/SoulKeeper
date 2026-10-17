package ru.astrainteractive.soulkeeper.module.souls.soul.usecase

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul

internal class GetNearestSoulUseCase(
    private val soulRepository: SoulRepository
) {
    suspend operator fun invoke(player: OnlineKPlayer): DatabaseSoul? {
        return soulRepository.getSoulsNear(player.getLocation(), PICK_UP_RADIUS)
            .getOrNull()
            .orEmpty()
            .firstOrNull { soul -> soul.isAvailableTo(player.uuid) }
    }

    private companion object {
        const val PICK_UP_RADIUS = 2
    }
}
