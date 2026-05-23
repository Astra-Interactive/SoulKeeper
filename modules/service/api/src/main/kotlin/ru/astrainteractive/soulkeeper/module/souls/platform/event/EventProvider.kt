package ru.astrainteractive.soulkeeper.module.souls.platform.event

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.soulkeeper.module.souls.platform.event.model.SharedPlayerJoinEvent
import ru.astrainteractive.soulkeeper.module.souls.platform.event.model.SharedPlayerLeaveEvent
import ru.astrainteractive.soulkeeper.module.souls.platform.event.model.SharedPlayerMoveEvent

interface EventProvider {
    fun getPlayerMoveEvent(): Flow<SharedPlayerMoveEvent>
    fun getPlayerJoinEvent(): Flow<SharedPlayerJoinEvent>
    fun getPlayerLeaveEvent(): Flow<SharedPlayerLeaveEvent>
}
