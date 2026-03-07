package ru.astrainteractive.soulkeeper.module.souls.migration

import java.io.File
import java.nio.file.Files
import java.util.UUID

/**
 * Previously, krates were stored in [dataFolder]
 * Now we want to store them inside [kratesFolder]
 */
class KrateFolderMigration(
    private val dataFolder: File,
    private val kratesFolder: File
) {

    suspend fun migrate() {
        if (!kratesFolder.exists()) kratesFolder.mkdirs()
        dataFolder.listFiles().orEmpty()
            .filter { file -> file.isDirectory }
            .filter { file ->
                val uuidOrNull = runCatching {
                    file.nameWithoutExtension
                        .split("_")
                        .firstOrNull()
                        ?.let(UUID::fromString)
                }.getOrNull()
                uuidOrNull != null
            }
            .onEach { file ->
                Files.move(
                    file.toPath(),
                    kratesFolder.resolve(file.name).toPath()
                )
            }
    }
}