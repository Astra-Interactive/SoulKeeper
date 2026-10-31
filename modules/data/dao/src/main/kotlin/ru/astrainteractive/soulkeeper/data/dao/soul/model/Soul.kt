package ru.astrainteractive.soulkeeper.data.dao.soul.model

import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.klibs.mikro.extensions.serialization.JUuidSerializer
import ru.astrainteractive.klibs.mikro.extensions.serialization.KInstantSerializer
import java.util.UUID
import kotlin.time.Instant

sealed interface Soul {
    val ownerUUID: UUID
    val ownerLastName: String
    val createdAt: Instant
    val isFree: Boolean
    val location: KLocation
    val hasItems: Boolean
    val exp: Int

    fun isAvailableTo(playerUuid: UUID): Boolean = isFree || ownerUUID == playerUuid
}

data class DatabaseSoul(
    val id: SoulId,
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: KLocation,
    override val hasItems: Boolean,
    override val exp: Int,
) : Soul

data class ItemDatabaseSoul(
    val id: SoulId,
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: KLocation,
    override val hasItems: Boolean,
    override val exp: Int,
    val items: List<StringFormatObject>
) : Soul

@Serializable
data class DefaultSoul(
    @Serializable(with = JUuidSerializer::class)
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    @Serializable(with = KInstantSerializer::class)
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: KLocation,
    override val exp: Int,
    val items: List<StringFormatObject>
) : Soul {
    override val hasItems: Boolean = items.isNotEmpty()
}
