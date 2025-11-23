package ru.astrainteractive.soulkeeper.module.souls.server.event

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.soulkeeper.module.souls.server.event.model.SharedPlayerJoinEvent
import ru.astrainteractive.soulkeeper.module.souls.server.event.model.SharedPlayerLeaveEvent
import ru.astrainteractive.soulkeeper.module.souls.server.event.model.SharedPlayerMoveEvent

interface EventProvider {
    fun getPlayerMoveEvent(): Flow<SharedPlayerMoveEvent>
    fun getPlayerJoinEvent(): Flow<SharedPlayerJoinEvent>
    fun getPlayerLeaveEvent(): Flow<SharedPlayerLeaveEvent>
}
