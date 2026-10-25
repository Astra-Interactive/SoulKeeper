package ru.astrainteractive.soulkeeper.data.dao.soul.util

import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject

fun ItemDatabaseSoul.hasExp(): Boolean = exp > 0

fun ItemDatabaseSoul.withoutExp(): ItemDatabaseSoul = copy(exp = 0)

fun ItemDatabaseSoul.withItems(items: List<StringFormatObject>): ItemDatabaseSoul {
    return copy(items = items, hasItems = items.isNotEmpty())
}
