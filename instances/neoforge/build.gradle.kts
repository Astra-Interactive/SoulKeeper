plugins {
    kotlin("jvm")
    alias(libs.plugins.neoforgegradle)
}

dependencies {
    implementation(projects.modules.commandNeoforge)
    implementation(projects.modules.serviceNeoforge)
    implementation(projects.modules.eventNeoforge)
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
