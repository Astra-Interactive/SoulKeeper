package ru.astrainteractive.aspekt.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import java.util.Timer
import kotlin.time.Duration

abstract class LifecycleCoroutineWorker(val key: String) : Lifecycle {
    private var scheduler: Timer? = null

    protected open val scope: CoroutineScope = CoroutineFeature.Default(Dispatchers.IO)

    data class Config(
        val delay: Duration,
        val initialDelay: Duration,
        val isEnabled: Boolean = true,
    )

    protected abstract fun getConfig(): Config

    protected abstract fun execute()

    private fun stopTimer() {
        scheduler?.cancel()
        scheduler = null
    }

    private fun startTimer() {
        val config = getConfig()
        if (!config.isEnabled) return
        scheduler = kotlin.concurrent.timer(
            name = key,
            daemon = false,
            initialDelay = config.initialDelay.inWholeMilliseconds,
            period = config.delay.inWholeMilliseconds,
            action = { execute() }
        )
    }

    override fun onDisable() {
        stopTimer()
        scope.cancel()
    }

    override fun onEnable() {
        startTimer()
    }

    override fun onReload() {
        stopTimer()
        scope.coroutineContext.cancelChildren()
    }
}
