@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.command.souls.util

import ru.astrainteractive.astralibs.command.api.brigadier.sender.KPlayerKCommandSender
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.permission.Permission
import ru.astrainteractive.soulkeeper.command.souls.fake.FakeConsoleKCommandSender
import ru.astrainteractive.soulkeeper.command.souls.fake.FakeOnlineKPlayer
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginPermission
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class KCommandSenderExtTest {
    private val ownerUuid = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e")
    private val strangerUuid = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private val soul = DatabaseSoul(
        id = 1L,
        ownerUUID = ownerUuid,
        ownerLastName = "Owner",
        createdAt = Instant.fromEpochSeconds(0),
        isFree = false,
        location = KLocation(
            x = 0.0,
            y = 64.0,
            z = 0.0,
            worldName = "world"
        ),
        hasItems = true,
        exp = 0
    )

    private fun player(uuid: UUID, permissions: Set<Permission> = emptySet()): KPlayerKCommandSender {
        return KPlayerKCommandSender(FakeOnlineKPlayer(uuid, permissions))
    }

    @Test
    fun GIVEN_soul_owner_without_permission_WHEN_freeing_own_soul_THEN_allowed() {
        assertTrue(player(ownerUuid).canFreeSoul(soul))
    }

    @Test
    fun GIVEN_stranger_without_permission_WHEN_freeing_soul_THEN_denied() {
        assertFalse(player(strangerUuid).canFreeSoul(soul))
    }

    @Test
    fun GIVEN_stranger_with_free_all_permission_WHEN_freeing_soul_THEN_allowed() {
        assertTrue(player(strangerUuid, setOf(PluginPermission.FreeAllSouls)).canFreeSoul(soul))
    }

    @Test
    fun GIVEN_console_with_free_all_permission_WHEN_freeing_soul_THEN_allowed() {
        assertTrue(FakeConsoleKCommandSender(setOf(PluginPermission.FreeAllSouls)).canFreeSoul(soul))
    }

    @Test
    fun GIVEN_console_without_permission_WHEN_freeing_soul_THEN_denied() {
        assertFalse(FakeConsoleKCommandSender(emptySet()).canFreeSoul(soul))
    }

    @Test
    fun GIVEN_player_with_teleport_permission_WHEN_teleporting_THEN_allowed() {
        assertTrue(player(strangerUuid, setOf(PluginPermission.TeleportToSouls)).canTeleportToSoul())
    }

    @Test
    fun GIVEN_player_without_teleport_permission_WHEN_teleporting_THEN_denied() {
        assertFalse(player(ownerUuid).canTeleportToSoul())
    }
}
