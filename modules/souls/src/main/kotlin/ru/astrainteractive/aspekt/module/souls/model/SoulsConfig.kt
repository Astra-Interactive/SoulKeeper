package ru.astrainteractive.aspekt.module.souls.model

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.exposed.model.DatabaseConfiguration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Serializable
internal data class SoulsConfig(
    @YamlComment("Type of database for souls information")
    @SerialName("database")
    val database: DatabaseConfiguration = DatabaseConfiguration.H2("souls"),
    @YamlComment("Soul will be public after this time")
    @SerialName("soul_free_after")
    val soulFreeAfter: Duration = 2.days,
    @YamlComment("After this time soul will disappear entirely")
    @SerialName("soul_fade_after")
    val soulFadeAfter: Duration = 14.days,
    @YamlComment("Near this radius saul will call for player")
    @SerialName("soul_call_radius")
    val soulCallRadius: Int = 64,
    @YamlComment(
        "Defines PVP behaviour",
        "NONE - the soul will be private",
        "EXP_ONLY - only exp will be public",
        "ITEMS_ONLY - only items will be public",
        "EXP_AND_ITEMS - exp and items will be public",
    )
    val pvpBehaviour: PvpBehaviour = PvpBehaviour.NONE,
    @YamlComment("The amount of xp will be retained in soul. [0.0, 1.0]")
    @SerialName("retained_xp")
    val retainedXp: Float = 1f,
    @SerialName("sounds")
    val sounds: Sounds = Sounds(),
    @SerialName("colors")
    val colors: Colors = Colors()

) {
    @Serializable
    data class Colors(
        val soulItems: Int = 0xFFFFFF,
        val soulXp: Int = 0x00FFFF,
        val soulGone: Int = 0xFFFF00,
        val soulCreated: Int = 0xeb3437
    )

    @Serializable
    data class Sounds(
        @SerialName("collect_xp")
        val collectXp: String = "entity.experience_orb.pickup",
        @SerialName("collect_item")
        val collectItem: String = "item.trident.return",
        @SerialName("soul_disappear")
        val soulDisappear: String = "entity.generic.extinguish_fire",
        @SerialName("soul_calling")
        val soulCalling: String = "block.beacon.ambient",
        @SerialName("soul_dropped")
        val soulDropped: String = "block.bell.resonate",
        @SerialName("soul_call_volume")
        val soulCallVolume: Float = 16f
    )

    @Serializable
    enum class PvpBehaviour {
        NONE, EXP_ONLY, ITEMS_ONLY, EXP_AND_ITEMS
    }
}
