package ru.astrainteractive.aspekt.job

import java.util.Timer

@Deprecated("Replaced by LifecycleCoroutineWorker")
abstract class ScheduledJob(val key: String) {
    private var scheduler: Timer? = null

    protected abstract val delayMillis: Long
    protected abstract val initialDelayMillis: Long
    protected abstract val isEnabled: Boolean

    protected abstract fun execute()

    open fun onEnable() {
        if (!isEnabled) return
        scheduler = kotlin.concurrent.timer(
            name = key,
            daemon = false,
            initialDelay = initialDelayMillis,
            period = delayMillis,
            action = { execute() }
        )
    }

    open fun onDisable() {
        scheduler?.cancel()
        scheduler = null
    }
}
