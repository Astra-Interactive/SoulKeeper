package ru.astrainteractive.aspekt.module.souls.database.model

import org.bukkit.Location
import java.time.Instant
import java.util.UUID

internal class Soul(
    val ownerUUID: UUID,
    val ownerName: String,
    val createdAt: Instant,
    val isFree: Boolean,
    val location: Location
)
