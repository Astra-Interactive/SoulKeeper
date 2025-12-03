import ru.astrainteractive.gradleplugin.property.extension.ModelPropertyValueExt.requireJinfo

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    alias(libs.plugins.neoforgegradle)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    // AstraLibs
    implementation(libs.minecraft.astralibs.core)
    implementation(libs.minecraft.astralibs.core.neoforge)
    implementation(libs.klibs.mikro.core)
    // klibs
    implementation(projects.modules.core)
    implementation(projects.modules.dao)
    implementation(projects.modules.service)
    implementation(projects.modules.serviceNeoforge)
}

dependencies {
    compileOnly(libs.minecraft.neoforgeversion)
}

tasks.withType<JavaCompile> {
    javaCompiler.set(
        javaToolchains.compilerFor {
            requireJinfo.jtarget.majorVersion
                .let(JavaLanguageVersion::of)
                .let(languageVersion::set)
        }
    )
}

configurations.runtimeElements {
    setExtendsFrom(emptySet())
}
