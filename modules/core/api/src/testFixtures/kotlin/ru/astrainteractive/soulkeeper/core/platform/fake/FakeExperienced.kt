package ru.astrainteractive.soulkeeper.core.platform.fake

import ru.astrainteractive.soulkeeper.core.platform.api.Experienced

class FakeExperienced : Experienced {
    val givenExperience = mutableListOf<Int>()

    override fun giveExperience(experience: Int) {
        givenExperience += experience
    }
}
