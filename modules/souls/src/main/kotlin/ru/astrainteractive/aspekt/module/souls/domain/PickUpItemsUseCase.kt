package ru.astrainteractive.aspekt.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.module.souls.util.playSound
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger

internal class PickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-PickUpItemsUseCase") {
    sealed interface Output {
        data object NoItemsPresent : Output
        data object ItemsCollected : Output
        data object SomeItemsRemain : Output
    }

    suspend fun invoke(player: Player, itemStackSoul: ItemStackSoul): Output {
        if (itemStackSoul.items.isEmpty()) return Output.NoItemsPresent

        val notAddedItems = player.inventory.addItem(*itemStackSoul.items.toTypedArray()).values.toList()
        if (notAddedItems != itemStackSoul.items) {
            itemStackSoul.soul.location.playSound(collectItemSoundProvider.invoke())
        }
        soulsDao.updateSoul(
            itemStackSoul.copy(
                exp = 0,
                items = notAddedItems
            )
        )
        return when {
            notAddedItems.isEmpty() -> Output.ItemsCollected
            else -> Output.SomeItemsRemain
        }
    }
}
