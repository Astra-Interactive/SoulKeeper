package ru.astrainteractive.soulkeeper.module.souls.dao

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers

@Suppress("VariableNaming")
internal class TestKotlinDispatchers(
    dispatcher: CoroutineDispatcher
) : KotlinDispatchers {
    override val Main: MainCoroutineDispatcher
        get() = Dispatchers.Main
    override val IO: CoroutineDispatcher = dispatcher
    override val Default: CoroutineDispatcher = dispatcher
    override val Unconfined: CoroutineDispatcher = dispatcher
}
