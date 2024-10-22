package ru.astrainteractive.soulkeeper.core.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent

fun Component?.orEmpty() = this ?: Component.empty()

fun Component.isEmpty() = this == Component.empty()

fun Component.isNotEmpty() = this != Component.empty()

fun Component.clickable(onClick: (Audience) -> Unit): Component {
    return this.clickEvent(
        ClickEvent.callback { audience ->
            onClick.invoke(audience)
        }
    )
}
