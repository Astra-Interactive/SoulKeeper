package ru.astrainteractive.soulkeeper.core.plugin

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.exposed.model.DatabaseConfiguration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Serializable
data class SoulsConfig(
    @YamlComment("Type of database for souls information")
    @SerialName("database")
    val database: DatabaseConfiguration = DatabaseConfiguration.H2("souls"),
    @YamlComment(
        "[DEFAULT] - 2 days",
        "Soul will be public after this time"
    )
    @SerialName("soul_free_after")
    val soulFreeAfter: Duration = 2.days,
    @YamlComment(
        "[DEFAULT] - 14 days",
        "After this time soul will disappear entirely"
    )
    @SerialName("soul_fade_after")
    val soulFadeAfter: Duration = 14.days,
    @YamlComment("Near this radius saul will call for player")
    @SerialName("soul_call_radius")
    val soulCallRadius: Int = 100,
    @YamlComment(
        "[NOT IMPLEMENTED]",
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
    @SerialName("particles")
    val particles: Particles = Particles(),
    @SerialName("display_soul_titles")
    @YamlComment(
        "[EXPERIMENTAL]",
        "This feature will display titles above souls via translations.yml -> souls.soul_of",
        "Need packet events"
    )
    val displaySoulTitles: Boolean = true,
    @YamlComment("Locate minimum Y coord of soul in the end dimension")
    val endLocationLimitY: Double = 0.0
) {
    @Serializable
    data class Particles(
        @SerialName("soul_items")
        val soulItems: Particle = Particle(
            key = "dust",
            count = 30,
            dustOptions = Particle.DustOptions(
                color = 0xFFFFFF,
                size = 2f
            )
        ),
        @SerialName("soul_xp")
        val soulXp: Particle = Particle(
            key = "dust",
            count = 30,
            dustOptions = Particle.DustOptions(
                color = 0x00FFFF,
                size = 2f
            )
        ),
        @SerialName("soul_gone")
        val soulGone: Particle = Particle(
            key = "dust",
            count = 128,
            dustOptions = Particle.DustOptions(
                color = 0xFFFF00,
                size = 32f
            )
        ),
        @SerialName("soul_created")
        val soulCreated: Particle = Particle(
            key = "dust",
            count = 128,
            dustOptions = Particle.DustOptions(
                color = 0xeb3437,
                size = 64f
            )
        ),
        @SerialName("soul_content_left")
        val soulContentLeft: Particle = Particle(
            key = "dust",
            count = 32,
            dustOptions = Particle.DustOptions(
                color = 0xa103fc,
                size = 32f
            )
        ),
    ) {
        @Serializable
        data class Particle(
            val key: String,
            val count: Int,
            @SerialName("dust_options")
            val dustOptions: DustOptions? = null
        ) {
            @Serializable
            data class DustOptions(
                val color: Int,
                val size: Float
            )
        }
    }

    @Serializable
    data class Sounds(
        @SerialName("collect_xp")
        val collectXp: SoundConfig = SoundConfig(
            id = "entity.experience_orb.pickup"
        ),
        @SerialName("collect_item")
        val collectItem: SoundConfig = SoundConfig(
            id = "item.trident.return"
        ),
        @SerialName("soul_disappear")
        val soulDisappear: SoundConfig = SoundConfig(
            id = "entity.generic.extinguish_fire"
        ),
        @SerialName("soul_dropped")
        val soulDropped: SoundConfig = SoundConfig(
            id = "block.bell.resonate"
        ),
        @SerialName("soul_calling")
        val calling: SoundConfig = SoundConfig(
            id = "block.beacon.ambient",
            volume = 16f
        ),
        @SerialName("soul_content_left")
        val soulContentLeft: SoundConfig = SoundConfig(
            id = "minecraft:block.anvil.place",
            volume = 16f
        ),
    ) {
        @Serializable
        data class SoundConfig(
            val id: String,
            val volume: Float = 1f,
            val pitch: Float = 0.75f
        )
    }

    @Serializable
    enum class PvpBehaviour {
        NONE, EXP_ONLY, ITEMS_ONLY, EXP_AND_ITEMS
    }
}
