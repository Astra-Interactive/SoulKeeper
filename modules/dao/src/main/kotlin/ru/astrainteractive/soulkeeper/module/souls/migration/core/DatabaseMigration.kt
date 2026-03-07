package ru.astrainteractive.soulkeeper.module.souls.migration.core

import org.jetbrains.exposed.sql.Database

interface DatabaseMigration {
    val requiredDbVersion: Int
    suspend fun migrate(database: Database)
}
