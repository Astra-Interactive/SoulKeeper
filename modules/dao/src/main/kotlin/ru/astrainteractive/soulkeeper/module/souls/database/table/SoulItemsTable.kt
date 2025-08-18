package ru.astrainteractive.soulkeeper.module.souls.database.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import ru.astrainteractive.soulkeeper.module.souls.database.coulmn.StringFormatObjectColumnType

internal object SoulItemsTable : LongIdTable(name = "SOUL_ITEMS") {
    val soulId = reference(
        name = "soul_id",
        foreign = SoulTable,
        onDelete = ReferenceOption.CASCADE
    )

    val itemStack = registerColumn("item_stack", StringFormatObjectColumnType())
}
