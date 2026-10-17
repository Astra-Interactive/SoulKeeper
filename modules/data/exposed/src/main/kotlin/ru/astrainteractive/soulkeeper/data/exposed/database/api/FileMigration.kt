package ru.astrainteractive.soulkeeper.data.exposed.database.api

internal interface FileMigration {
    suspend fun migrate()
}
