package ru.astrainteractive.aspekt.job

import java.util.Timer

abstract class ScheduledJob(val key: String) {
    private var scheduler: Timer? = null
    protected abstract val delayMillis: Long
    protected abstract val initialDelayMillis: Long
    protected abstract val isEnabled: Boolean
    protected abstract fun execute()

    open fun onEnable() {
        if (!isEnabled) return
        scheduler = kotlin.concurrent.timer(key, false, initialDelayMillis, delayMillis) {
            execute()
        }
    }

    open fun onDisable() {
        scheduler?.cancel()
        scheduler = null
    }
}
