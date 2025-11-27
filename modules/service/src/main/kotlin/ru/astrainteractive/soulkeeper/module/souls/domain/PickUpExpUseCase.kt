package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced

class PickUpExpUseCase(
    private val collectXpSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter,
    private val experiencedFactory: Experienced.Factory<OnlineMinecraftPlayer>
) {
    sealed interface Output {
        data object NoExpPresent : Output
        data object ExpCollected : Output
    }

    suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.exp <= 0) return Output.NoExpPresent
        effectEmitter.playSoundForPlayer(
            location = soul.location,
            player = player,
            sound = collectXpSoundProvider.invoke()
        )
        experiencedFactory.create(player).giveExperience(soul.exp)
        soulsDao.updateSoul(soul = soul.copy(exp = 0))
        return Output.ExpCollected
    }
}
