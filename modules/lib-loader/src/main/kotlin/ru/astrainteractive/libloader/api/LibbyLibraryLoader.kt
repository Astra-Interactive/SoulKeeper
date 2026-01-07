package ru.astrainteractive.libloader.api

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.logging.Logger
import ru.astrainteractive.libloader.api.LibLoader.Dependency
import ru.astrainteractive.libloader.async.JConcurrentExecutor

class LibbyLibraryLoader(
    private val libraryManager: LibraryManager,
    private val jConcurrentExecutor: JConcurrentExecutor
) : LibLoader {
    private val logger: Logger
        get() = libraryManager.logger

    private fun setupRepositories(repositories: List<LibLoader.Repository>) {
        repositories.forEach { repository ->
            when (repository) {
                LibLoader.Repository.MavenCentral -> libraryManager.addMavenCentral()
                is LibLoader.Repository.Url -> libraryManager.addRepository(repository.url)
            }
        }
    }

    private fun Library.Builder.excludeTransitiveDependencies(
        dependencies: List<Dependency>
    ) = this.apply {
        dependencies.forEach { library ->
            val (group, artifact) = library.dependency.split(":")
            excludeTransitiveDependency(group, artifact)
        }
    }

    private fun mapLibrary(
        library: LibLoader.Library,
        excludedTransitiveDependencies: List<Dependency>
    ): Library {
        val (group, artifact, version) = library.dependency.split(":")
        return Library.builder()
            .groupId(group.replace(".", "{}"))
            .artifactId(artifact)
            .version(version)
            .resolveTransitiveDependencies(library.resolveTransitiveDependencies)
            .apply {
                if (!library.resolveTransitiveDependencies) return@apply
                excludeTransitiveDependencies(excludedTransitiveDependencies)
            }
            .apply {
                val relocate = library.relocate ?: return@apply
                relocate(
                    relocate.relocatePattern.replace(".", "{}"),
                    relocate.relocatedPatternPrefix
                        .plus(".")
                        .plus(relocate.relocatePattern)
                        .replace(".", " {}")
                )
            }
            .build()
    }

    private fun loadLibraries(
        libraries: List<LibLoader.Library>,
        excludedTransitiveDependencies: List<Dependency>
    ) {
        with(jConcurrentExecutor) {
            libraries.forEachParallel { library ->
                logger.debug("Loading library $library")
                try {
                    libraryManager.loadLibrary(mapLibrary(library, excludedTransitiveDependencies))
                } catch (e: Exception) {
                    logger.error("Failed to load library $library", e)
                }
            }
        }
    }

    override fun loadAll(
        libraries: List<LibLoader.Library>,
        excludedTransitiveDependencies: List<Dependency>,
        repositories: List<LibLoader.Repository>
    ) {
        logger.info("Loading ${libraries.size} libraries...")
        setupRepositories(repositories)
        loadLibraries(libraries, excludedTransitiveDependencies)
        logger.info("Libraries loaded!")
    }
}
