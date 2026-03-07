package ru.astrainteractive.soulkeeper.module.souls.database.table

import org.jetbrains.exposed.dao.id.LongIdTable
import ru.astrainteractive.soulkeeper.module.souls.database.coulmn.KJavaInstantColumnType

internal object SoulTable : LongIdTable(name = "SOUL") {
    val ownerUUID = text("owner_uuid")
    val ownerLastName = text("owner_last_name")

    val created_at = registerColumn("created_at_millis", KJavaInstantColumnType())

    val isFree = bool("is_free")
    val exp = integer("exp")

    val locationWorld = text("location_world")
    val locationX = double("location_x")
    val locationY = double("location_y")
    val locationZ = double("location_z")
}
