package ru.astrainteractive.aspekt.module.souls.model

import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.exposed.model.DatabaseConfiguration

@Serializable
internal data class SoulsConfig(
    val database: DatabaseConfiguration = DatabaseConfiguration.H2("souls"),
)
