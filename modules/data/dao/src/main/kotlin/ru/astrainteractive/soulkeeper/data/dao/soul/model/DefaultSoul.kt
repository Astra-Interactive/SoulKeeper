package ru.astrainteractive.soulkeeper.data.dao.soul.model

import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.klibs.mikro.extensions.serialization.JUuidSerializer
import ru.astrainteractive.klibs.mikro.extensions.serialization.KInstantSerializer
import java.util.UUID
import kotlin.time.Instant

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
