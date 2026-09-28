@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.command.soulkrate

import ru.astrainteractive.astralibs.command.api.exception.ArgumentConverterException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UuidArgumentConverterTest {
    @Test
    fun GIVEN_uuid_of_a_soul_owner_WHEN_converted_THEN_returns_that_uuid() {
        val ownerUuid = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e")

        assertEquals(ownerUuid, UuidArgumentConverter.transform(ownerUuid.toString()))
    }

    @Test
    fun GIVEN_malformed_uuid_WHEN_converted_THEN_fails_as_invalid_argument() {
        assertFailsWith<ArgumentConverterException> {
            UuidArgumentConverter.transform("not-a-uuid")
        }
    }
}
