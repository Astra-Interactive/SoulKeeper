package ru.astrainteractive.soulkeeper.data.exposed.soul.table

import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject

internal class StringFormatObjectColumnType : ColumnType<StringFormatObject>() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.textType()

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
