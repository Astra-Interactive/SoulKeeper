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
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import java.util.Optional
import java.util.UUID

internal class PacketEventsShowArmorStandUseCase(
    kyoriKrate: CachedKrate<KyoriComponentSerializer>,
    translationKrate: CachedKrate<PluginTranslation>
) : ShowArmorStandUseCase {
    private val kyori by kyoriKrate
    private val translation by translationKrate

    override fun generateEntityId(): Int {
        return SpigotReflectionUtil.generateEntityId()
    }

    private fun destroy(player: Player, ids: Collection<Int>) {
        val packet = WrapperPlayServerDestroyEntities(*ids.toIntArray())
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }

    private fun show(id: Int, player: Player, soul: Soul) {
        val bukkitLocation = soul.location.toBukkitLocation()
        val vector3d = bukkitLocation.toVector().toVector3d()
        val packet = WrapperPlayServerSpawnEntity(
            id,
            Optional.of(UUID.randomUUID()),
            EntityTypes.ARMOR_STAND,
            Vector3d(
                vector3d.x,
                vector3d.y,
                vector3d.z
            ),
            bukkitLocation.pitch,
            bukkitLocation.yaw,
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

    override fun destroy(
        player: OnlineMinecraftPlayer,
        ids: Collection<Int>
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        destroy(
            bukkitPlayer,
            ids
        )
    }

    override fun show(
        id: Int,
        player: OnlineMinecraftPlayer,
        soul: Soul
    ) {
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return
        show(
            id,
            bukkitPlayer,
            soul
        )
    }
}
