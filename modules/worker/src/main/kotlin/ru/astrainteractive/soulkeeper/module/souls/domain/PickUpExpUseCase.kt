package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul

internal class PickUpExpUseCase(
    private val collectXpSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-PickUpExpUseCase") {
    sealed interface Output {
        data object NoExpPresent : Output
        data object ExpCollected : Output
    }

    suspend fun invoke(player: Player, soul: ItemDatabaseSoul): Output {
        if (soul.exp <= 0) {
            return Output.NoExpPresent
        }
        soul.location.toBukkitLocation().playSoundForPlayer(player, collectXpSoundProvider.invoke())
        player.giveExp(soul.exp)
        soulsDao.updateSoul(soul = soul.copy(exp = 0))
        return Output.ExpCollected
    }
}
