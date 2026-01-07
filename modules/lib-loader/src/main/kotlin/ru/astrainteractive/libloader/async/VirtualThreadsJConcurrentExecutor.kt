package ru.astrainteractive.libloader.async

import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.use

object VirtualThreadsJConcurrentExecutor : JConcurrentExecutor {
    override fun <T> List<T>.forEachParallel(block: (T) -> Unit) {
        val chunks = this
        Executors
            .newVirtualThreadPerTaskExecutor()
            .use { executor ->
                chunks
                    .map { chunk ->
                        executor.submit {
                            block.invoke(chunk)
                        }
                    }
                    .forEach(Future<*>::get)
            }
    }
}
