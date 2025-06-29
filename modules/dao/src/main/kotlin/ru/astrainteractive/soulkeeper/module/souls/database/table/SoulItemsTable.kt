package ru.astrainteractive.soulkeeper.module.souls.database.table

import org.jetbrains.exposed.dao.id.LongIdTable
import ru.astrainteractive.soulkeeper.module.souls.database.coulmn.StringFormatObjectColumnType

internal object SoulItemsTable : LongIdTable(name = "SOUL_ITEMS") {
    val soulId = reference("soul_id", SoulTable)

    val itemStack = registerColumn("item_stack", StringFormatObjectColumnType())
}
