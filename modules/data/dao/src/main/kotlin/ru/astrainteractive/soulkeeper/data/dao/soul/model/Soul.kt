package ru.astrainteractive.soulkeeper.data.dao.soul.model

import ru.astrainteractive.astralibs.server.location.KLocation
import java.util.UUID
import kotlin.time.Instant

interface Soul {
    val ownerUUID: UUID
    val ownerLastName: String
    val createdAt: Instant
    val isFree: Boolean
    val location: KLocation
    val hasItems: Boolean
    val exp: Int

    fun isAvailableTo(playerUuid: UUID): Boolean = isFree || ownerUUID == playerUuid
}
