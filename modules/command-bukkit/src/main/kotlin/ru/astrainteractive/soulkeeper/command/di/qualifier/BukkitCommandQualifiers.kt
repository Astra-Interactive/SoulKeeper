package ru.astrainteractive.soulkeeper.command.di.qualifier

import dev.zacsweers.metro.Qualifier

@Qualifier
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class BukkitCommandLifecycle
