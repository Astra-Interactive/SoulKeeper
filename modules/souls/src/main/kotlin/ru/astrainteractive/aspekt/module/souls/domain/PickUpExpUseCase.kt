package ru.astrainteractive.aspekt.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.module.souls.util.playSound
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger

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
        collectXpSoundProvider.invoke().playSound(itemStackSoul.soul.location)
        player.giveExp(itemStackSoul.exp)
        soulsDao.updateSoul(
            itemStackSoul.copy(
                exp = 0,
                soul = itemStackSoul.soul.copy(
                    hasXp = false
                )
            )
        )
        return Output.ExpCollected
    }
}
