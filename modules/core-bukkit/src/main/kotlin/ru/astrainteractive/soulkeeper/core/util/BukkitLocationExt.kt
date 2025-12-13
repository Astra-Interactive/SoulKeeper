package ru.astrainteractive.soulkeeper.core.util

import ru.astrainteractive.astralibs.server.location.Location

fun org.bukkit.Location.toDatabaseLocation(): Location {
    return Location(
        x = x,
        y = y,
        z = z,
        worldName = world.name
    )
}
