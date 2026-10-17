package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import net.minecraft.world.item.ItemStack
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.MinecraftUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase.Output
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.util.addItems

internal class NeoForgePickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulRepository: SoulRepository,
    private val effectEmitter: EffectEmitter,
    private val isDeadPlayerProvider: IsDeadPlayerProvider,
    private val dispatchers: KotlinDispatchers
) : PickUpItemsUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpItemsUseCase") {

    override suspend fun invoke(player: OnlineKPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.items.isEmpty()) return Output.NoItemsPresent
        val serverPlayer = MinecraftUtil.getOnlinePlayer(player.uuid) ?: return Output.SomeItemsRemain

        if (serverPlayer.abilities.instabuild) return Output.SomeItemsRemain
        if (serverPlayer.gameMode.isCreative) return Output.SomeItemsRemain
        if (isDeadPlayerProvider.isDead(player)) return Output.SomeItemsRemain

        val items = soul.items
            .map(StringFormatObject::raw)
            .map(ItemStackSerializer::decodeFromString)
            .mapNotNull { result -> result.getOrNull() }
        val requestedAmount = items.sumOf(ItemStack::getCount)
        val notAddedItems = withContext(dispatchers.Main) {
            player.addItems(
                items = items,
                isDead = isDeadPlayerProvider::isDead
            )
        }
        if (notAddedItems.sumOf(ItemStack::getCount) < requestedAmount) {
            withContext(dispatchers.Main) {
                effectEmitter.playSoundForPlayer(
                    location = soul.location,
                    player = player,
                    sound = collectItemSoundProvider.invoke()
                )
            }
        }
        soulRepository.updateSoul(
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
