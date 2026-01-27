package ru.astrainteractive.soulkeeper.module.souls.krate

import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.util.parse
import ru.astrainteractive.astralibs.util.writeIntoFile
import ru.astrainteractive.klibs.kstorage.suspend.SuspendMutableKrate
import ru.astrainteractive.klibs.kstorage.suspend.impl.DefaultSuspendMutableKrate
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import java.io.File
import java.time.Instant
import java.util.UUID

class PlayerSoulKrate(
    stringFormat: StringFormat,
    private val dataFolder: File,
    private val createdAt: Instant,
    private val ownerUUID: UUID,
    private val readIndex: Int = 0
) : SuspendMutableKrate<DefaultSoul?>, Logger by JUtiltLogger("SoulKeeper-PlayerSoulKrate") {
    private fun createFile(): File {
        val parentDir = dataFolder.resolve("$ownerUUID")
        parentDir.mkdirs()

        var index = 0
        var file: File
        do {
            file = parentDir.resolve("${createdAt.epochSecond}_$index.yml")
            index++
        } while (file.exists())
        file.createNewFile()
        return file
    }

    private val krate = DefaultSuspendMutableKrate(
        factory = { null },
        loader = {
            val file = dataFolder
                .resolve("$ownerUUID")
                .resolve("${createdAt.epochSecond}_$readIndex.yml")
            stringFormat.parse<DefaultSoul>(file)
                .onFailure { error(it) { "Failed to load soul for $ownerUUID" } }
                .getOrNull()
        },
        saver = saver@{ defaultSoul ->
            val file = createFile()
            if (defaultSoul == null) {
                file.renameTo(file.resolveSibling("${file.nameWithoutExtension}.deleted"))
                return@saver
            }
            stringFormat.writeIntoFile(defaultSoul, file)
        }
    )

    override suspend fun save(value: DefaultSoul?) {
        krate.save(value)
    }

    override suspend fun reset() {
        krate.reset()
    }

    override suspend fun getValue(): DefaultSoul? {
        return krate.getValue()
    }
}
