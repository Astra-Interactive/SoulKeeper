package ru.astrainteractive.soulkeeper.module.souls.domain

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase.Output

internal class PickUpExpUseCaseBukkit(
    private val collectXpSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
) : PickUpExpUseCase,
    Logger by JUtiltLogger("AspeKt-PickUpExpUseCase") {

    override suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.exp <= 0) {
            return Output.NoExpPresent
        }
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return Output.NoExpPresent
        soul.location.toBukkitLocation().playSoundForPlayer(bukkitPlayer, collectXpSoundProvider.invoke())
        bukkitPlayer.giveExp(soul.exp)
        soulsDao.updateSoul(soul = soul.copy(exp = 0))
        return Output.ExpCollected
    }
}
