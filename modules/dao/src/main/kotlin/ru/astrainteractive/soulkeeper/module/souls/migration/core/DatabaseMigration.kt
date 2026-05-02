package ru.astrainteractive.soulkeeper.module.souls.migration.core

import org.jetbrains.exposed.v1.jdbc.Database

interface DatabaseMigration {
    val requiredDbVersion: Int
    suspend fun migrate(database: Database)
}
