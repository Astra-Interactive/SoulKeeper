package ru.astrainteractive.soulkeeper.data.exposed.soul.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

internal object SoulItemsTable : LongIdTable(name = "SOUL_ITEMS") {
    val soulId = reference(
        name = "soul_id",
        foreign = SoulTable,
        onDelete = ReferenceOption.CASCADE
    )

    val itemStack = registerColumn("item_stack", StringFormatObjectColumnType())
}
