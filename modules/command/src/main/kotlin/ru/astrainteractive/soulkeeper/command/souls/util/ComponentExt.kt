package ru.astrainteractive.soulkeeper.command.souls.util

import net.kyori.adventure.text.Component
import ru.astrainteractive.astralibs.localization.component.isEmpty

internal fun Component.append(
    other: Component?,
    addSpace: Boolean
): Component {
    return if (other == null || other.isEmpty()) {
        this
    } else if (addSpace) {
        this.appendSpace().append(other)
    } else {
        this.append(other)
    }
}
