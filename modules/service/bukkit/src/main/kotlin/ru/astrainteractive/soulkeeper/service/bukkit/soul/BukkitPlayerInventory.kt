package ru.astrainteractive.soulkeeper.service.bukkit.soul

import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.serialization.ItemStackSerializer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.service.api.soul.model.AddItemsResult

internal class BukkitPlayerInventory :
    PlayerInventory,
    Logger by JUtiltLogger("SoulKeeper-BukkitPlayerInventory") {

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
        return Bukkit.getPlayer(player.uuid) != null
    }

    override fun addItems(player: OnlineKPlayer, items: List<StringFormatObject>): AddItemsResult {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid)
            ?: return AddItemsResult(leftovers = items, isAnyItemAdded = false)
        val itemStacks = decode(items)
        val requestedAmount = itemStacks.sumOf(ItemStack::getAmount)

        @Suppress("SpreadOperator")
        val leftovers = bukkitPlayer.inventory
            .addItem(*itemStacks.toTypedArray())
            .values
            .toList()
        return AddItemsResult(
            leftovers = leftovers
                .map(ItemStackSerializer::encodeToString)
                .map(::StringFormatObject),
            isAnyItemAdded = leftovers.sumOf(ItemStack::getAmount) < requestedAmount
        )
    }
}
