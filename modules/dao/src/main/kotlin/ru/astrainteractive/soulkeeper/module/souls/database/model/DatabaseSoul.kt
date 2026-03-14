package ru.astrainteractive.soulkeeper.module.souls.database.model

import ru.astrainteractive.astralibs.server.location.KLocation
import java.time.Instant
import java.util.UUID

data class DatabaseSoul(
    val id: Long,
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: KLocation,
    override val hasItems: Boolean,
    override val exp: Int,
) : Soul
