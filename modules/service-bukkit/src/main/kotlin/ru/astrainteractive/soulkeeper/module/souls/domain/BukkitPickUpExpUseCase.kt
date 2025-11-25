package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase.Output
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter

internal class BukkitPickUpExpUseCase(
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
        Bukkit.getPlayer(player.uuid)?.giveExp(soul.exp)
        soulsDao.updateSoul(soul = soul.copy(exp = 0))
        return Output.ExpCollected
    }
}
