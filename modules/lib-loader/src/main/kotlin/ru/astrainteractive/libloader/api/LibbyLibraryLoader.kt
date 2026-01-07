package ru.astrainteractive.libloader.api

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.logging.Logger
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

    private fun mapLibrary(library: LibLoader.Library): Library {
        val (group, artifact, version) = library.dependency.split(":")
        return Library.builder()
            .groupId(group.replace(".", "{}"))
            .artifactId(artifact)
            .version(version)
            .resolveTransitiveDependencies(library.resolveTransitiveDependencies)
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

    private fun loadLibraries(libraries: List<LibLoader.Library>) {
        with(jConcurrentExecutor) {
            libraries.forEachParallel { library ->
                logger.debug("Loading library $library")
                try {
                    libraryManager.loadLibrary(mapLibrary(library))
                } catch (e: Exception) {
                    logger.error("Failed to load library $library", e)
                }
            }
        }
    }

    override fun loadAll(libraries: List<LibLoader.Library>, repositories: List<LibLoader.Repository>) {
        logger.info("Loading ${libraries.size} libraries...")
        setupRepositories(repositories)
        loadLibraries(libraries)
        logger.info("Libraries loaded!")
    }
}
