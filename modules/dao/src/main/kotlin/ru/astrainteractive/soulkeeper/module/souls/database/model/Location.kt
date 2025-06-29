package ru.astrainteractive.soulkeeper.module.souls.database.model

import kotlin.math.pow
import kotlin.math.sqrt

data class Location(
    val x: Double,
    val y: Double,
    val z: Double,
    val worldName: String
) {
    fun distance(other: Location): Double {
        return sqrt((x - other.x).pow(2.0) + (y - other.y).pow(2.0) + (z - other.z).pow(2.0))
    }
}