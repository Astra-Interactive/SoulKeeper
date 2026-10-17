package ru.astrainteractive.soulkeeper.module.souls.soul.model

sealed interface PickUpSoulResult {
    data object SomethingRest : PickUpSoulResult
    data object AllPickedUp : PickUpSoulResult
}
