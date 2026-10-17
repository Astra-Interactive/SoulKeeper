package ru.astrainteractive.soulkeeper.core.platform.api

interface Experienced {
    fun giveExperience(experience: Int)

    fun interface Factory<T> {
        fun create(owner: T): Experienced
    }
}
