package network.bahn.gitviewer.data

import java.io.File

data class RepoFsEntry(
    val name: String,
    val isDirectory: Boolean,
    val relativePath: String
)

/** App-owned clone tree: `{filesDir}/git-repos/{repoId}/…` — never device aliases. */
class RepoWorkspace(private val filesDir: File) {

    val root: File = File(filesDir, "git-repos").also { it.mkdirs() }

    fun dirFor(repo: RepoEntity): File = File(root, repo.id.toString())

    fun resolve(repo: RepoEntity, relativePath: String): File {
        var current = dirFor(repo)
        for (segment in segments(relativePath)) {
            current = File(current, segment)
        }
        return current
    }

    fun list(repo: RepoEntity, relativePath: String): List<RepoFsEntry> {
        val dir = resolve(repo, relativePath)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()
            .orEmpty()
            .filter { it.name != "." && it.name != ".." }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .map { file ->
                RepoFsEntry(
                    name = file.name,
                    isDirectory = file.isDirectory,
                    relativePath = join(relativePath, file.name)
                )
            }
    }

    fun readText(repo: RepoEntity, relativePath: String): String {
        return resolve(repo, relativePath).readText()
    }

    fun delete(repo: RepoEntity) {
        dirFor(repo).deleteRecursively()
    }

    /** Move any older clone location into `{git-repos}/{id}`. */
    fun migrateIfNeeded(repo: RepoEntity, extraLegacyRoots: List<File> = emptyList()) {
        val dest = dirFor(repo)
        if (dest.exists()) return
        val legacy = buildList {
            val stored = File(repo.localPath)
            if (stored.exists()) add(stored)
            extraLegacyRoots.forEach { root ->
                val named = File(root, stored.name)
                if (named.exists()) add(named)
            }
        }.firstOrNull { it.exists() && it.isDirectory } ?: return
        dest.parentFile?.mkdirs()
        if (!legacy.renameTo(dest)) {
            legacy.copyRecursively(dest, overwrite = false)
            legacy.deleteRecursively()
        }
    }

    companion object {
        fun segments(relativePath: String): List<String> =
            relativePath.split('/', '\\')
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != "." && it != ".." }

        fun join(parent: String, name: String): String {
            val parts = segments(parent) + name
            return parts.joinToString("/")
        }

        fun breadcrumb(repoName: String, relativePath: String): String {
            val parts = listOf(repoName) + segments(relativePath)
            return parts.joinToString(" / ")
        }
    }
}
