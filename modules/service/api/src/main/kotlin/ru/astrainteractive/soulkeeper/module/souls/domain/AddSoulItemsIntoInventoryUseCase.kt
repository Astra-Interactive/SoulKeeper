package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DefaultSoul

interface AddSoulItemsIntoInventoryUseCase {
    suspend fun invoke(player: OnlineKPlayer, soul: DefaultSoul)
}
