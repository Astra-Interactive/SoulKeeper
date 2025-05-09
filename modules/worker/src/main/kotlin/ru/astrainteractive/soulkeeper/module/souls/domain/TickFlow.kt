package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Duration

class TickFlow(duration: Duration) : Flow<Unit> by flow(
    block = {
        while (currentCoroutineContext().isActive) {
            delay(duration)
            emit(Unit)
        }
    }
)
