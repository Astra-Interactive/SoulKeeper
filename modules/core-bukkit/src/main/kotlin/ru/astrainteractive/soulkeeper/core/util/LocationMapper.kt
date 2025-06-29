package ru.astrainteractive.soulkeeper.core.util

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.location.Location
import org.bukkit.Location as BLocation

fun Location.toBukkitLocation(): BLocation {
    return BLocation(
        Bukkit.getWorld(worldName),
        x,
        y,
        z
    )
}

fun BLocation.toDatabaseLocation(): Location {
    return Location(
        x = x,
        y = y,
        z = z,
        worldName = world.name
    )
}
