@file:Suppress("RolelessClassName")

package ru.astrainteractive.soulkeeper.data.exposed.database.api

import org.jetbrains.exposed.v1.jdbc.Database

internal interface DatabaseMigration {
    val requiredDbVersion: Int
    suspend fun migrate(database: Database)
}
