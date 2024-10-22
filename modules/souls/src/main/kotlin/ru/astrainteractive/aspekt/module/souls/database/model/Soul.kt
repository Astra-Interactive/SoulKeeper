package ru.astrainteractive.aspekt.module.souls.database.model

import org.bukkit.Location
import java.time.Instant
import java.util.UUID

internal interface Soul {
    val ownerUUID: UUID
    val ownerLastName: String
    val createdAt: Instant
    val isFree: Boolean
    val location: Location
    val hasItems: Boolean
    val hasXp: Boolean
}
