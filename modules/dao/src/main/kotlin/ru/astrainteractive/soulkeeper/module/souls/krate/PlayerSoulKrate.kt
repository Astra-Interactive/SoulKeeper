package ru.astrainteractive.soulkeeper.module.souls.krate

import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.util.parse
import ru.astrainteractive.astralibs.util.writeIntoFile
import ru.astrainteractive.klibs.kstorage.suspend.SuspendMutableKrate
import ru.astrainteractive.klibs.kstorage.suspend.impl.DefaultSuspendMutableKrate
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import java.io.File
import java.time.Instant
import java.util.UUID

class PlayerSoulKrate(
    stringFormat: StringFormat,
    private val dataFolder: File,
    private val createdAt: Instant,
    private val ownerUUID: UUID
) : SuspendMutableKrate<Soul?>, Logger by JUtiltLogger("SoulKeeper-PlayerSoulKrate") {
    private fun getFile(shouldCreate: Boolean = false): File {
        val parentDir = dataFolder.resolve("$ownerUUID")
        if (shouldCreate) parentDir.mkdirs()

        var index = 0
        var file: File
        do {
            file = parentDir.resolve("${createdAt.epochSecond}_$index.yml")
            index++
        } while (file.exists() && shouldCreate)
        if (shouldCreate) file.createNewFile()
        return file
    }

    private val krate = DefaultSuspendMutableKrate<Soul?>(
        factory = { null },
        loader = {
            val file = getFile(shouldCreate = false)
            stringFormat.parse<DefaultSoul>(file)
                .onFailure { error(it) { "Failed to load soul for $ownerUUID" } }
                .getOrNull()
        },
        saver = saver@{ soul ->
            val file = getFile(shouldCreate = true)
            if (soul == null) {
                file.renameTo(file.resolveSibling("${file.nameWithoutExtension}.deleted"))
                return@saver
            }
            val defaultSoul = DefaultSoul(
                ownerUUID = soul.ownerUUID,
                ownerLastName = soul.ownerLastName,
                createdAt = soul.createdAt,
                isFree = soul.isFree,
                location = soul.location,
                exp = soul.exp,
                items = when (val localSoul = soul) {
                    is ItemDatabaseSoul -> localSoul.items
                    is DefaultSoul -> localSoul.items
                    else -> {
                        error { "Unknown soul type" }
                        emptyList()
                    }
                }
            )
            stringFormat.writeIntoFile(defaultSoul, file)
        }
    )

    override suspend fun save(value: Soul?) {
        krate.save(value)
    }

    override suspend fun reset() {
        krate.reset()
    }

    override suspend fun getValue(): Soul? {
        return krate.getValue()
    }
}
