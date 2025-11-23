package ru.astrainteractive.soulkeeper.module.souls.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.server.util.asAstraLocation
import ru.astrainteractive.astralibs.server.util.asOnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.server.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.server.event.model.SharedPlayerJoinEvent
import ru.astrainteractive.soulkeeper.module.souls.server.event.model.SharedPlayerLeaveEvent
import ru.astrainteractive.soulkeeper.module.souls.server.event.model.SharedPlayerMoveEvent

class EventProviderBukkit(
    private val plugin: JavaPlugin
) : EventProvider {
    override fun getPlayerMoveEvent(): Flow<SharedPlayerMoveEvent> {
        return flowEvent<PlayerMoveEvent>(plugin)
            .filterIsInstance<PlayerMoveEvent>()
            .map { event ->
                SharedPlayerMoveEvent(
                    player = event.player.asOnlineMinecraftPlayer(),
                    to = event.player.location.asAstraLocation()
                )
            }
    }

    override fun getPlayerJoinEvent(): Flow<SharedPlayerJoinEvent> {
        return flowEvent<PlayerJoinEvent>(plugin)
            .filterIsInstance<PlayerJoinEvent>()
            .map { event -> SharedPlayerJoinEvent(event.player.asOnlineMinecraftPlayer()) }
    }

    override fun getPlayerLeaveEvent(): Flow<SharedPlayerLeaveEvent> {
        return flowEvent<PlayerQuitEvent>(plugin)
            .filterIsInstance<PlayerQuitEvent>()
            .map { event -> SharedPlayerLeaveEvent(event.player.asOnlineMinecraftPlayer()) }
    }
}
