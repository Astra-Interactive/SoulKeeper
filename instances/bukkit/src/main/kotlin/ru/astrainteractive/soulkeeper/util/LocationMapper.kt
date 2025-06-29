package ru.astrainteractive.soulkeeper.util

import org.bukkit.Bukkit
import ru.astrainteractive.soulkeeper.module.souls.database.model.Location


fun Location.toBukkitLocation(): org.bukkit.Location {
    return org.bukkit.Location(
        Bukkit.getWorld(worldName),
        x,
        y,
        z
    )
}

fun org.bukkit.Location.toDatabaseLocation(): Location {
    return Location(
        x = x,
        y = y,
        z = z,
        worldName = world.name
    )
}