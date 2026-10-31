package ru.astrainteractive.soulkeeper.data.dao.soul.model

class SoulNotFoundException(
    soulId: SoulId
) : Exception("Soul with id ${soulId.value} not found")
