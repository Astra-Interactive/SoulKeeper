package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul

interface PickUpItemsUseCase {
    sealed interface Output {
        data object NoItemsPresent : Output
        data object ItemsCollected : Output
        data object SomeItemsRemain : Output
    }

    suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output
}
