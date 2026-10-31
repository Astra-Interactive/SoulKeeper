@file:Suppress("FunctionVerb")

package ru.astrainteractive.soulkeeper.service.api.platform.api

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.soulkeeper.service.api.platform.model.SharedPlayerJoinEvent
import ru.astrainteractive.soulkeeper.service.api.platform.model.SharedPlayerLeaveEvent
import ru.astrainteractive.soulkeeper.service.api.platform.model.SharedPlayerMoveEvent

interface EventProvider {
    fun getPlayerMoveEvent(): Flow<SharedPlayerMoveEvent>
    fun getPlayerJoinEvent(): Flow<SharedPlayerJoinEvent>
    fun getPlayerLeaveEvent(): Flow<SharedPlayerLeaveEvent>
}
