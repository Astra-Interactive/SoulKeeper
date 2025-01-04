package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul

internal class PickUpExpUseCase(
    private val collectXpSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
) : Logger by JUtiltLogger("AspeKt-PickUpExpUseCase") {
    sealed interface Output {
        data object NoExpPresent : Output
        data object ExpCollected : Output
    }

    suspend fun invoke(player: Player, bukkitSoul: BukkitSoul): Output {
        if (bukkitSoul.exp <= 0) {
            return Output.NoExpPresent
        }
        bukkitSoul.location.playSoundForPlayer(player, collectXpSoundProvider.invoke())
        player.giveExp(bukkitSoul.exp)
        soulsDao.updateSoul(
            soul = bukkitSoul.copy(
                exp = 0,
            )
        )
        return Output.ExpCollected
    }
}
