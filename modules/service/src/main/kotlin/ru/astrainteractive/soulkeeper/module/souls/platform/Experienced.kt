package ru.astrainteractive.soulkeeper.module.souls.platform

interface Experienced {
    fun giveExperience(experience: Int)

    fun interface Factory<T> {
        fun create(owner: T): Experienced
    }
}
