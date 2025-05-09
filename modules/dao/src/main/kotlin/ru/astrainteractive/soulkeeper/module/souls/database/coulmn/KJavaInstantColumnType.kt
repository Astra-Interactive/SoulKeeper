package ru.astrainteractive.soulkeeper.module.souls.database.coulmn

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.time.Instant

class KJavaInstantColumnType : ColumnType<Instant>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.longType()

    override fun valueFromDB(value: Any): Instant = when (value) {
        is Long -> Instant.ofEpochMilli(value)
        is Number -> Instant.ofEpochMilli(value.toLong())
        is String -> Instant.ofEpochMilli(value.toLong())
        else -> error("Unexpected value of type Long: $value of ${value::class.qualifiedName}")
    }

    override fun valueToDB(value: Instant?): Any? {
        return value?.toEpochMilli()
    }

    override fun valueToString(value: Instant?): String {
        return value?.toEpochMilli()?.toString().orEmpty()
    }

    override fun nonNullValueToString(value: Instant): String {
        return value.toEpochMilli().toString()
    }
}
