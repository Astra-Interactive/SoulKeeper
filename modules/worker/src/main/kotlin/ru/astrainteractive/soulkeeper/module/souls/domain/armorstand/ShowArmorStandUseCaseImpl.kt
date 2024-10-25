package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.util.getValue
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import java.util.Optional
import java.util.UUID

internal class ShowArmorStandUseCaseImpl(
    kyoriKrate: Krate<KyoriComponentSerializer>,
    translationKrate: Krate<PluginTranslation>
) : ShowArmorStandUseCase {
    private val kyori by kyoriKrate
    private val translation by translationKrate

    override fun generateEntityId(): Int {
        return SpigotReflectionUtil.generateEntityId()
    }

    override fun destroy(player: Player, ids: Collection<Int>) {
        val packet = WrapperPlayServerDestroyEntities(*ids.toIntArray())
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }

    override fun show(id: Int, player: Player, soul: Soul) {
        val vector3d = soul.location.toVector().toVector3d()
        val packet = WrapperPlayServerSpawnEntity(
            id,
            Optional.of(UUID.randomUUID()),
            EntityTypes.ARMOR_STAND,
            Vector3d(
                vector3d.x,
                vector3d.y,
                vector3d.z
            ),
            soul.location.pitch,
            soul.location.yaw,
            0f,
            0,
            Optional.empty(),
        )
        val metadata = WrapperPlayServerEntityMetadata(
            id,
            listOf(
                // Invisible
                EntityData(
                    0,
                    EntityDataTypes.BYTE,
                    (0x20 * 1).toByte()
                ),
                EntityData(
                    2,
                    EntityDataTypes.OPTIONAL_ADV_COMPONENT,
                    Optional.of(translation.souls.soulOf(soul.ownerLastName).let(kyori::toComponent))
                ),
                // Show custom name
                EntityData(
                    3,
                    EntityDataTypes.BOOLEAN,
                    true
                ),
                // No Gravity
                EntityData(
                    5,
                    EntityDataTypes.BOOLEAN,
                    true
                ),
            )
        )
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
        PacketEvents.getAPI().playerManager.sendPacket(player, metadata)
    }
}
