plugins {
    id("org.jetbrains.kotlin.jvm")
    id("ru.astrainteractive.gradleplugin.java.version")
}
dependencies {
    api(libs.libby.core)
    api(libs.libby.standalone)
}
