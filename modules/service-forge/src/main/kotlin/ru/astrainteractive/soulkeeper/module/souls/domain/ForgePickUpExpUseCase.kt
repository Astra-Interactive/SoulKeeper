package ru.astrainteractive.soulkeeper.module.souls.domain

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.ForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase.Output
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter

internal class ForgePickUpExpUseCase(
    private val collectXpSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter
) : PickUpExpUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpExpUseCase") {

    override suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.exp <= 0) return Output.NoExpPresent
        effectEmitter.playSoundForPlayer(
            location = soul.location,
            player = player,
            sound = collectXpSoundProvider.invoke()
        )
        ForgeUtil.getOnlinePlayer(player.uuid)?.giveExperiencePoints(soul.exp)
        soulsDao.updateSoul(soul.copy(exp = 0))
        return Output.ExpCollected
    }
}
