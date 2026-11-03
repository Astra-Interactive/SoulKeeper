package ru.astrainteractive.soulkeeper.service.api.soul.model

import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject

data class AddItemsResult(
    val leftovers: List<StringFormatObject>,
    val isAnyItemAdded: Boolean
)
