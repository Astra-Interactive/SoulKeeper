package ru.astrainteractive.soulkeeper.data.exposed.soul.table

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import kotlin.time.Instant

internal class KJavaInstantColumnType : ColumnType<Instant>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.longType()

    override fun valueFromDB(value: Any): Instant {
        return when (value) {
            is Long -> Instant.fromEpochMilliseconds(value)
            is Number -> Instant.fromEpochMilliseconds(value.toLong())
            is String -> Instant.fromEpochMilliseconds(value.toLong())
            else -> error("Unexpected value of type Long: $value of ${value::class.qualifiedName}")
        }
    }

    override fun valueToDB(value: Instant?): Any? {
        return value?.toEpochMilliseconds()
    }

    override fun valueToString(value: Instant?): String {
        return value?.toEpochMilliseconds()?.toString().orEmpty()
    }

    override fun nonNullValueToString(value: Instant): String {
        return value.toEpochMilliseconds().toString()
    }
}
