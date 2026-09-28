package ru.astrainteractive.soulkeeper.command.soulkrate

import ru.astrainteractive.astralibs.command.api.argumenttype.ArgumentConverter
import ru.astrainteractive.astralibs.command.api.exception.ArgumentConverterException
import java.util.UUID

/** Converts a string argument to a [UUID]; a malformed one fails as an invalid argument, not as a crash. */
internal data object UuidArgumentConverter : ArgumentConverter<UUID> {
    override fun transform(argument: String): UUID {
        return runCatching { UUID.fromString(argument) }
            .getOrElse { _ -> throw ArgumentConverterException(UuidArgumentConverter::class.java, argument) }
    }
}
