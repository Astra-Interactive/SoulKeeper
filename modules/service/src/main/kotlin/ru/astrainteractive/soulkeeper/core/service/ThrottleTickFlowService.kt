package ru.astrainteractive.soulkeeper.core.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import ru.astrainteractive.astralibs.coroutines.withTimings
import ru.astrainteractive.astralibs.service.Service
import ru.astrainteractive.astralibs.service.ServiceExecutor
import ru.astrainteractive.klibs.mikro.core.coroutines.CoroutineFeature
import ru.astrainteractive.klibs.mikro.core.coroutines.TickFlow
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

class ThrottleTickFlowService(
    coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO,
    private val delay: Flow<Duration>,
    private val initialDelay: Flow<Duration> = flowOf(Duration.ZERO),
    private val executor: ServiceExecutor
) : Service {
    private val scope = CoroutineFeature
        .Default(coroutineContext)
        .withTimings()

    fun <T, K> Flow<T>.throttleLatest(
        transform: suspend (T) -> K
    ): Flow<K> = channelFlow {
        val scope = this
        var job: Job? = null

        collectLatest { value ->
            job?.join()
            job = scope.launch {
                send(transform(value))
            }
        }
    }

    private val lazyTickFlow = lazy {
        combine(delay, initialDelay, ::TickFlow)
            .flattenConcat()
            .throttleLatest { executor.doWork() }
            .launchIn(scope)
    }

    override fun onCreate() {
        lazyTickFlow.value
    }

    override fun onDestroy() {
        lazyTickFlow.value.cancel()
        scope.cancel()
    }
}
