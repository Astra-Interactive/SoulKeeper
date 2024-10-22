package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSound
import ru.astrainteractive.soulkeeper.module.souls.database.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemStackSoul

internal class PickUpExpUseCase(
    private val collectXpSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-PickUpExpUseCase") {
    sealed interface Output {
        data object NoExpPresent : Output
        data object ExpCollected : Output
    }

    suspend fun invoke(player: Player, itemStackSoul: ItemStackSoul): Output {
        if (itemStackSoul.exp <= 0) {
            return Output.NoExpPresent
        }
        itemStackSoul.location.playSound(collectXpSoundProvider.invoke())
        player.giveExp(itemStackSoul.exp)
        soulsDao.updateSoul(
            itemStackSoul.copy(
                exp = 0,
            )
        )
        return Output.ExpCollected
    }
}
