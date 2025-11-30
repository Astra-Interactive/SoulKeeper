package ru.astrainteractive.soulkeeper.module.souls.domain

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.astralibs.server.util.ForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase.Output
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer

internal class NeoForgePickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter
) : PickUpItemsUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpItemsUseCase") {
    /**
     * @param items List of items to add to inventory
     * @return not fitted items into inventory
     */
    private fun Inventory.addItems(items: List<ItemStack>): List<ItemStack> {
        return items
            .mapNotNull { stack ->
                add(stack)
                stack.copy()
            }
            .filterNot(ItemStack::isEmpty)
            .also { setChanged() }
    }

    override suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.items.isEmpty()) return Output.NoItemsPresent
        val serverPlayer = ForgeUtil.getOnlinePlayer(player.uuid) ?: return Output.SomeItemsRemain
        if (serverPlayer.abilities.instabuild) return Output.SomeItemsRemain
        if (serverPlayer.gameMode.isCreative) return Output.SomeItemsRemain

        val notAddedItems = serverPlayer.inventory
            .addItems(
                items = soul.items
                    .map(StringFormatObject::raw)
                    .map(ItemStackSerializer::decodeFromString)
                    .mapNotNull { result -> result.getOrNull() }
            )
        if (notAddedItems.isEmpty()) {
            effectEmitter.playSoundForPlayer(soul.location, player, collectItemSoundProvider.invoke())
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
