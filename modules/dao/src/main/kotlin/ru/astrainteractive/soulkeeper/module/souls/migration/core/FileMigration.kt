package ru.astrainteractive.soulkeeper.module.souls.migration.core

interface FileMigration {
    suspend fun migrate()
}
