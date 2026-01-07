package ru.astrainteractive.libloader.api

interface LibLoader {
    @JvmInline
    value class Dependency(val dependency: String)

    data class Library(
        val dependency: String,
        val relocate: Relocate? = null,
        val resolveTransitiveDependencies: Boolean = true
    ) {
        data class Relocate(
            val relocatePattern: String,
            val relocatedPatternPrefix: String,
        )
    }

    sealed interface Repository {
        data class Url(val url: String) : Repository
        data object MavenCentral : Repository
    }

    fun loadAll(
        libraries: List<Library>,
        excludedTransitiveDependencies: List<Dependency>,
        repositories: List<Repository>
    )
}
