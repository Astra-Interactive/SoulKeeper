package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul

interface PickUpExpUseCase {
    sealed interface Output {
        data object NoExpPresent : Output
        data object ExpCollected : Output
    }

    suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output
}
