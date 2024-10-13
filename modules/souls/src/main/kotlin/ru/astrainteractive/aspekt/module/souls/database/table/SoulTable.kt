package ru.astrainteractive.aspekt.module.souls.database.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

internal object SoulTable : LongIdTable(name = "SOUL") {
    val ownerUUID = text("owner_uuid")
    val ownerLastName = text("owner_last_name")

    val created_at = timestamp("created_at")

    val isFree = bool("is_free")

    val locationWorld = text("location_world")
    val locationX = double("location_x")
    val locationY = double("location_y")
    val locationZ = double("location_z")
}
