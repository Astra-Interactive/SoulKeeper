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

internal class ForgePickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val effectEmitter: EffectEmitter
) : PickUpItemsUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpItemsUseCase") {
    /**
     * todo fix
     * @return items not added into inventory
     */
    private fun Inventory.addItem(items: List<ItemStack>): List<ItemStack> {
        val inventory = this
        return buildList {
            items.forEach { item ->
                var remaining = item.count
                while (remaining > 0) {
                    val singleStack = item.copy().apply { count = minOf(remaining, maxStackSize) }
                    val addedIntoInventory = inventory.add(singleStack)
                    if (!addedIntoInventory) add(singleStack)
                    remaining -= singleStack.count
                }
            }
        }.filterNotNull()
    }

    override suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.items.isEmpty()) return Output.NoItemsPresent
        val serverPlayer = ForgeUtil.getOnlinePlayer(player.uuid) ?: return Output.SomeItemsRemain

        val items: List<ItemStack> = soul.items
            .map(StringFormatObject::raw)
            .map(ItemStackSerializer::decodeFromString)
            .mapNotNull { it.getOrNull() }

        val notAddedItems = serverPlayer.inventory.addItem(items)
        if (notAddedItems.size != items.size) {
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
