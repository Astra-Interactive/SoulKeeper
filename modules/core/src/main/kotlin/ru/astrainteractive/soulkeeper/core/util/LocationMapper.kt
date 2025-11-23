package ru.astrainteractive.soulkeeper.core.util

import ru.astrainteractive.astralibs.server.location.Location

fun Location.toDatabaseLocation(): Location {
    return Location(
        x = x,
        y = y,
        z = z,
        worldName = worldName
    )
}
