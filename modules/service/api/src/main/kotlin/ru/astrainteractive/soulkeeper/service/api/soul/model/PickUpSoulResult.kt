package ru.astrainteractive.soulkeeper.service.api.soul.model

sealed interface PickUpSoulResult {
    data object SomethingRest : PickUpSoulResult
    data object AllPickedUp : PickUpSoulResult
}
