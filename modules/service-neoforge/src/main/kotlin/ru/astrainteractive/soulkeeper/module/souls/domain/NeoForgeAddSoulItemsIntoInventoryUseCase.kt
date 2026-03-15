package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.NeoForgeOnlineKPlayer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.util.addItems

class NeoForgeAddSoulItemsIntoInventoryUseCase(
    private val isDeadPlayerProvider: IsDeadPlayerProvider
) :
    AddSoulItemsIntoInventoryUseCase,
    Logger by JUtiltLogger("SoulKeeper-AddSoulItemsIntoInventoryUseCase") {

    override suspend fun invoke(
        player: OnlineKPlayer,
        soul: DefaultSoul
    ) {
        require(player is NeoForgeOnlineKPlayer) {
            "Player must be BukkitOnlineKPlayer"
        }
        val items = soul?.items
            .orEmpty()
            .map(StringFormatObject::raw)
            .map(ItemStackSerializer::decodeFromString)
            .mapNotNull { itemStackResult ->
                itemStackResult
                    .onFailure { error(it) { "Failed to deserialize item stack" } }
                    .getOrNull()
            }
        player.addItems(items, isDeadPlayerProvider::isDead)
    }
}
