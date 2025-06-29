package ru.astrainteractive.soulkeeper.module.souls.database.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import ru.astrainteractive.soulkeeper.module.souls.database.coulmn.KJavaInstantColumnType
import ru.astrainteractive.soulkeeper.module.souls.database.coulmn.StringFormatObjectColumnType
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject

internal object SoulItemsTable : LongIdTable(name = "SOUL_ITEMS") {
    val soulId = reference("soul_id", SoulTable)

    val itemStack = registerColumn("item_stack", StringFormatObjectColumnType())
}
