package ru.astrainteractive.soulkeeper.data.dao.soul.model

import ru.astrainteractive.astralibs.server.location.KLocation
import java.time.Instant
import java.util.UUID

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
) : Soul {
    fun hasExp(): Boolean = exp > 0

    fun withoutExp(): ItemDatabaseSoul = copy(exp = 0)

    fun withItems(items: List<StringFormatObject>): ItemDatabaseSoul {
        return copy(items = items, hasItems = items.isNotEmpty())
    }
}
