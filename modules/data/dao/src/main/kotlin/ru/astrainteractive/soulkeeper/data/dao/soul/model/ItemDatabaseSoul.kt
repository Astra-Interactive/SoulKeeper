package ru.astrainteractive.soulkeeper.data.dao.soul.model

import ru.astrainteractive.astralibs.server.location.KLocation
import java.util.UUID
import kotlin.time.Instant

data class ItemDatabaseSoul(
    val id: Long,
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: KLocation,
    override val hasItems: Boolean,
    override val exp: Int,
    val items: List<StringFormatObject>
) : Soul
