package ru.astrainteractive.soulkeeper.module.souls.database.coulmn

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.vendors.currentDialect
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject

internal class StringFormatObjectColumnType : ColumnType<StringFormatObject>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.longType()

    override fun valueFromDB(value: Any): StringFormatObject {
        return StringFormatObject(value.toString())
    }

    override fun valueToDB(value: StringFormatObject?): Any? {
        return value?.raw
    }

    override fun valueToString(value: StringFormatObject?): String {
        return value?.raw.orEmpty()
    }

    override fun nonNullValueToString(value: StringFormatObject): String {
        return value.raw
    }
}
