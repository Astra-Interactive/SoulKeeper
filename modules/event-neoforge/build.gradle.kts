plugins {
    kotlin("jvm")
    alias(libs.plugins.neoforgegradle)
}

dependencies {
    implementation(projects.modules.serviceNeoforge)
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
