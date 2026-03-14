package ru.astrainteractive.soulkeeper.module.souls.database.model

import ru.astrainteractive.astralibs.server.location.KLocation
import java.time.Instant
import java.util.UUID

interface Soul {
    val ownerUUID: UUID
    val ownerLastName: String
    val createdAt: Instant
    val isFree: Boolean
    val location: KLocation
    val hasItems: Boolean
    val exp: Int
}
