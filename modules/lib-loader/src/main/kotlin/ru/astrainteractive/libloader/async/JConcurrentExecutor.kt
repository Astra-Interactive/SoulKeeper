package ru.astrainteractive.libloader.async

interface JConcurrentExecutor {
    fun <T> List<T>.forEachParallel(block: (T) -> Unit)
}
