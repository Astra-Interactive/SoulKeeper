package ru.astrainteractive.soulkeeper.core.platform.api

interface Experienced {
    fun giveExperience(experience: Int)

    fun interface Resolver<T> {
        fun create(owner: T): Experienced
    }
}
