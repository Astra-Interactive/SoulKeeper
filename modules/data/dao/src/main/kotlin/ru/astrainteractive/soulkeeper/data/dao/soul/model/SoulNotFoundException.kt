package ru.astrainteractive.soulkeeper.data.dao.soul.model

class SoulNotFoundException(
    soulId: Long
) : Exception("Soul with id $soulId not found")
