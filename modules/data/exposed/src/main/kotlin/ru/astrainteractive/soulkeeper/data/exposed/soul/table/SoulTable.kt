package ru.astrainteractive.soulkeeper.data.exposed.soul.table

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

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
