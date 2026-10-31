package ru.astrainteractive.soulkeeper.service.api.soul.fake

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.service.api.soul.model.AddItemsResult

internal class FakePlayerInventory : PlayerInventory {
    var canPickUp: Boolean = true
    var freeSlots: Int = Int.MAX_VALUE

    val receivedItems = mutableListOf<StringFormatObject>()

    override fun canPickUpItems(player: OnlineKPlayer): Boolean = canPickUp

    override fun addItems(player: OnlineKPlayer, items: List<StringFormatObject>): AddItemsResult {
        val added = items.take(freeSlots)
        receivedItems += added
        return AddItemsResult(
            leftovers = items.drop(added.size),
            isAnyItemAdded = added.isNotEmpty()
        )
    }
}
