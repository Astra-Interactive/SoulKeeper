package ru.astrainteractive.soulkeeper.module.souls.database.model

import org.bukkit.Location
import java.time.Instant
import java.util.UUID

internal data class DatabaseSoul(
    val id: Long,
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: Location,
    override val hasItems: Boolean,
    override val hasXp: Boolean,
) : Soul
