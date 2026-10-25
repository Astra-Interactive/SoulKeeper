package ru.astrainteractive.soulkeeper.data.exposed.database.migration

import ru.astrainteractive.klibs.mikro.core.coroutines.propagateCancellationException
import ru.astrainteractive.soulkeeper.data.exposed.database.api.FileMigration
import java.io.File
import java.nio.file.Files
import java.util.UUID

/**
 * Previously, krates were stored in [dataFolder]
 * Now we want to store them inside [kratesFolder]
 */
internal class KrateFolderMigration(
    private val dataFolder: File,
    private val kratesFolder: File
) : FileMigration {

    override suspend fun migrate() {
        if (kratesFolder.exists()) return
        if (!kratesFolder.exists()) kratesFolder.mkdirs()
        dataFolder.listFiles().orEmpty()
            .filter { file -> file.isDirectory }
            .filter { file ->
                val uuidOrNull = runCatching {
                    file.nameWithoutExtension
                        .split("_")
                        .firstOrNull()
                        ?.let(UUID::fromString)
                }.propagateCancellationException().getOrNull()
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
