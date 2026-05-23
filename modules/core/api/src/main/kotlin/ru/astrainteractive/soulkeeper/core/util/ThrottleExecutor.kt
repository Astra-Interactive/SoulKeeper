package ru.astrainteractive.soulkeeper.core.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ThrottleExecutor(private val duration: Duration) {
    private val now: Duration
        get() = System.currentTimeMillis().milliseconds

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
