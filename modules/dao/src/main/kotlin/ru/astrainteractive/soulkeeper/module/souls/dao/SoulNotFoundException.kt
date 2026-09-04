package ru.astrainteractive.soulkeeper.module.souls.dao

class SoulNotFoundException(
    soulId: Long
) : Exception("Soul with id $soulId not found")
