package ru.astrainteractive.soulkeeper.core.platform.fake

import ru.astrainteractive.soulkeeper.core.platform.Experienced

class FakeExperienced : Experienced {
    val givenExperience = mutableListOf<Int>()

    override fun giveExperience(experience: Int) {
        givenExperience += experience
    }
}
