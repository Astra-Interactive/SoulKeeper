package ru.astrainteractive.soulkeeper.module.souls.soul.api

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.soul.model.AddItemsResult

interface PlayerInventory {
    fun canPickUpItems(player: OnlineKPlayer): Boolean

    fun addItems(player: OnlineKPlayer, items: List<StringFormatObject>): AddItemsResult
}
