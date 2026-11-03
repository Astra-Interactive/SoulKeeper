@file:Suppress("RolelessClassName")

package ru.astrainteractive.soulkeeper.data.exposed.database.api

internal interface DatabaseMigration {
    val requiredDbVersion: Int
    suspend fun migrate()
}
