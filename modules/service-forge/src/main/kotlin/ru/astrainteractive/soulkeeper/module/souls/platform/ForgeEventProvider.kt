package ru.astrainteractive.soulkeeper.module.souls.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.server.util.asLocatable
import ru.astrainteractive.astralibs.server.util.asOnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.model.SharedPlayerJoinEvent
import ru.astrainteractive.soulkeeper.module.souls.platform.event.model.SharedPlayerLeaveEvent
import ru.astrainteractive.soulkeeper.module.souls.platform.event.model.SharedPlayerMoveEvent

object ForgeEventProvider : EventProvider {
    override fun getPlayerMoveEvent(): Flow<SharedPlayerMoveEvent> {
        return flowEvent<TickEvent.PlayerTickEvent>()
            .filter { it.phase == TickEvent.Phase.END }
            .mapNotNull { event ->
                val serverPlayer = event.player
                    .tryCast<ServerPlayer>()
                    ?: return@mapNotNull null
                SharedPlayerMoveEvent(
                    player = serverPlayer.asOnlineMinecraftPlayer(),
                    to = serverPlayer.asLocatable().getLocation()
                )
            }
    }

    override fun getPlayerJoinEvent(): Flow<SharedPlayerJoinEvent> {
        return flowEvent<PlayerEvent.PlayerLoggedInEvent>()
            .mapNotNull { evt ->
                val serverPlayer = evt.entity
                    .tryCast<ServerPlayer>()
                    ?: return@mapNotNull null
                SharedPlayerJoinEvent(serverPlayer.asOnlineMinecraftPlayer())
            }
    }

    override fun getPlayerLeaveEvent(): Flow<SharedPlayerLeaveEvent> {
        return flowEvent<PlayerEvent.PlayerLoggedOutEvent>()
            .mapNotNull { evt ->
                val serverPlayer = evt.entity
                    .tryCast<ServerPlayer>()
                    ?: return@mapNotNull null
                SharedPlayerLeaveEvent(serverPlayer.asOnlineMinecraftPlayer())
            }
    }
}