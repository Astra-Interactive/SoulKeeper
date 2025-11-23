package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import java.util.Optional
import java.util.UUID

internal class ShowArmorStandUseCaseImpl(
    kyoriKrate: CachedKrate<KyoriComponentSerializer>,
    translationKrate: CachedKrate<PluginTranslation>
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
}
