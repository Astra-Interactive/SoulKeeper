package ru.astrainteractive.soulkeeper.module.souls.database.model

import java.time.Instant
import java.util.UUID

data class DefaultSoul(
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: Location,
    override val hasItems: Boolean,
    override val exp: Int,
    val items: List<StringFormatObject>
) : Soul
