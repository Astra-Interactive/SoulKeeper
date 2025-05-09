package ru.astrainteractive.soulkeeper.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

inline fun <reified T, R> combineInstantly(
    vararg flows: Flow<T>,
    crossinline transform: (Array<T?>) -> R
): Flow<R> {
    return channelFlow {
        val array = arrayOfNulls<T>(flows.size)
        flows.forEachIndexed { index, flow ->
            launch {
                flow.collect {
                    array[index] = it
                    send(transform.invoke(array))
                }
            }
        }
    }
}

inline fun <reified T1, reified T2, R> combineInstantly(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    crossinline transform: (T1?, T2?) -> R,
): Flow<R> {
    return combineInstantly(flow, flow2) { array ->
        transform(
            array[0] as? T1,
            array[1] as? T2
        )
    }
}

inline fun <reified T1, reified T2, reified T3, R> combineInstantly(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    crossinline transform: (T1?, T2?, T3?) -> R,
): Flow<R> {
    return combineInstantly(flow, flow2, flow3) { array ->
        transform(
            array[0] as? T1,
            array[1] as? T2,
            array[2] as? T3
        )
    }
}
