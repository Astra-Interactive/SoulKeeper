package ru.astrainteractive.soulkeeper.module.souls.database.model

import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.klibs.mikro.extensions.serialization.JInstantSerializer
import ru.astrainteractive.klibs.mikro.extensions.serialization.JUuidSerializer
import java.time.Instant
import java.util.UUID

@Serializable
data class DefaultSoul(
    @Serializable(with = JUuidSerializer::class)
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    @Serializable(with = JInstantSerializer::class)
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: KLocation,
    override val exp: Int,
    val items: List<StringFormatObject>
) : Soul {
    override val hasItems: Boolean = items.isNotEmpty()
}
