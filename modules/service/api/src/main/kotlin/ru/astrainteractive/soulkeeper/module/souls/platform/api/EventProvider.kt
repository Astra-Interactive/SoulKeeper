@file:Suppress("FunctionVerb")

package ru.astrainteractive.soulkeeper.module.souls.platform.api

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.soulkeeper.module.souls.platform.model.SharedPlayerJoinEvent
import ru.astrainteractive.soulkeeper.module.souls.platform.model.SharedPlayerLeaveEvent
import ru.astrainteractive.soulkeeper.module.souls.platform.model.SharedPlayerMoveEvent

interface EventProvider {
    fun getPlayerMoveEvent(): Flow<SharedPlayerMoveEvent>
    fun getPlayerJoinEvent(): Flow<SharedPlayerJoinEvent>
    fun getPlayerLeaveEvent(): Flow<SharedPlayerLeaveEvent>
}
