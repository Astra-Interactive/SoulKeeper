package ru.astrainteractive.soulkeeper.core.util

import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.location.Location

fun Location.toBukkitLocation(): org.bukkit.Location {
    return org.bukkit.Location(
        Bukkit.getWorld(worldName),
        x,
        y,
        z
    )
}
