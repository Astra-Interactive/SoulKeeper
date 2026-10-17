package ru.astrainteractive.soulkeeper.module.souls.soul

import net.minecraft.world.item.ItemStack
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.MinecraftUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.platform.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.model.AddItemsResult
import ru.astrainteractive.soulkeeper.module.souls.util.addItems

internal class ForgePlayerInventory(
    private val isDeadPlayerProvider: IsDeadPlayerProvider
) : PlayerInventory,
    Logger by JUtiltLogger("SoulKeeper-ForgePlayerInventory") {

    private fun decode(items: List<StringFormatObject>): List<ItemStack> {
        return items
            .map(StringFormatObject::raw)
            .map(ItemStackSerializer::decodeFromString)
            .mapNotNull { itemStackResult ->
                itemStackResult
                    .onFailure { throwable -> error(throwable) { "Failed to deserialize item stack" } }
                    .getOrNull()
            }
    }

    override fun canPickUpItems(player: OnlineKPlayer): Boolean {
        val serverPlayer = MinecraftUtil.getOnlinePlayer(player.uuid) ?: return false
        if (serverPlayer.abilities.instabuild) return false
        if (serverPlayer.gameMode.isCreative) return false
        return !isDeadPlayerProvider.isDead(player)
    }

    override fun addItems(player: OnlineKPlayer, items: List<StringFormatObject>): AddItemsResult {
        val itemStacks = decode(items)
        val requestedAmount = itemStacks.sumOf(ItemStack::getCount)
        val leftovers = player.addItems(
            items = itemStacks,
            isDead = isDeadPlayerProvider::isDead
        )
        return AddItemsResult(
            leftovers = leftovers
                .map(ItemStackSerializer::encodeToString)
                .map(::StringFormatObject),
            isAnyItemAdded = leftovers.sumOf(ItemStack::getCount) < requestedAmount
        )
    }
}
