package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.NeoForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase.Output
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.util.addItems

internal class NeoForgePickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter,
    private val isDeadPlayerProvider: IsDeadPlayerProvider,
    private val dispatchers: KotlinDispatchers
) : PickUpItemsUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpItemsUseCase") {

    override suspend fun invoke(player: OnlineKPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.items.isEmpty()) return Output.NoItemsPresent
        val serverPlayer = NeoForgeUtil.getOnlinePlayer(player.uuid) ?: return Output.SomeItemsRemain

        if (serverPlayer.abilities.instabuild) return Output.SomeItemsRemain
        if (serverPlayer.gameMode.isCreative) return Output.SomeItemsRemain
        if (isDeadPlayerProvider.isDead(player)) return Output.SomeItemsRemain

        val notAddedItems = withContext(dispatchers.Main) {
            player.addItems(
                items = soul.items
                    .map(StringFormatObject::raw)
                    .map(ItemStackSerializer::decodeFromString)
                    .mapNotNull { result -> result.getOrNull() },
                isDead = isDeadPlayerProvider::isDead
            )
        }
        if (notAddedItems.isEmpty()) {
            withContext(dispatchers.Main) {
                effectEmitter.playSoundForPlayer(
                    location = soul.location,
                    player = player,
                    sound = collectItemSoundProvider.invoke()
                )
            }
        }
        soulsDao.updateSoul(
            soul.copy(
                items = notAddedItems
                    .map(ItemStackSerializer::encodeToString)
                    .map(::StringFormatObject)
            )
        )
        return when {
            notAddedItems.isEmpty() -> Output.ItemsCollected
            else -> Output.SomeItemsRemain
        }
    }
}
