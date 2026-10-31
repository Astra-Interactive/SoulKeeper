package ru.astrainteractive.soulkeeper.service.api.service

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class ThrottleExecutor(
    private val duration: Duration,
    private val clock: Clock
) {
    private val now: Duration
        get() = clock.now().toEpochMilliseconds().milliseconds

    private var lastSuspend = now

    fun isSuspended(): Boolean {
        return lastSuspend.plus(duration) > now
    }

    fun setSuspended() {
        lastSuspend = now
    }

    inline fun <T> execute(block: () -> T) {
        if (isSuspended()) return
        setSuspended()
        block.invoke()
    }
}
