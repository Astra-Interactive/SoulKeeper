package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSound
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul

internal class PickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-PickUpItemsUseCase") {
    sealed interface Output {
        data object NoItemsPresent : Output
        data object ItemsCollected : Output
        data object SomeItemsRemain : Output
    }

    suspend fun invoke(player: Player, bukkitSoul: BukkitSoul): Output {
        if (bukkitSoul.items.isEmpty()) return Output.NoItemsPresent

        val notAddedItems = player.inventory.addItem(*bukkitSoul.items.toTypedArray()).values.toList()
        if (notAddedItems != bukkitSoul.items) {
            bukkitSoul.location.playSound(collectItemSoundProvider.invoke())
        }
        soulsDao.updateSoul(
            bukkitSoul.copy(
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
