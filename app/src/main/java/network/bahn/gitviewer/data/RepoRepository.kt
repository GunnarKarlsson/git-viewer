package network.bahn.gitviewer.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class RepoRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).repoDao()
    private val keys = SshKeyStore(context)
    private val workspace = RepoWorkspace(context.filesDir)
    private val legacyReposDir = File(context.filesDir, "repos")

    fun getAllRepos(): Flow<List<RepoEntity>> = dao.getAll()

    suspend fun getRepo(id: Long) = dao.getById(id)

    fun hasSshKey(repoId: Long): Boolean = keys.hasKey(repoId)

    suspend fun sshPublicKey(repoId: Long): String? = withContext(Dispatchers.IO) {
        keys.publicKey(repoId)
    }

    suspend fun isCloned(repo: RepoEntity): Boolean = withContext(Dispatchers.IO) {
        workspace.migrateIfNeeded(repo, listOf(legacyReposDir))
        File(workspace.dirFor(repo), ".git").exists()
    }

    suspend fun listEntries(repo: RepoEntity, relativePath: String): List<RepoFsEntry> =
        withContext(Dispatchers.IO) {
            workspace.migrateIfNeeded(repo, listOf(legacyReposDir))
            workspace.list(repo, relativePath)
        }

    suspend fun readFile(repo: RepoEntity, relativePath: String): String =
        withContext(Dispatchers.IO) {
            workspace.migrateIfNeeded(repo, listOf(legacyReposDir))
            workspace.readText(repo, relativePath)
        }

    /** Add a new repo (does NOT clone yet). */
    suspend fun addRepo(name: String, url: String, generateSshKey: Boolean = false): Long {
        val id = dao.insert(RepoEntity(name = name, url = url, localPath = ""))
        dao.update(RepoEntity(id = id, name = name, url = url, localPath = id.toString()))
        if (generateSshKey) {
            withContext(Dispatchers.IO) {
                keys.generate(id)
            }
        }
        return id
    }

    /** Clone / pull the given repo. Updates lastPulled. */
    suspend fun pullRepo(repo: RepoEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SshEnvironment.install(context.filesDir)
            workspace.migrateIfNeeded(repo, listOf(legacyReposDir))
            val dir = workspace.dirFor(repo)
            val identity = keys.privateKeyFile(repo.id).takeIf { it.exists() }
            GitHelper.cloneOrPull(repo.url, dir, identity)
            dao.update(repo.copy(lastPulled = System.currentTimeMillis(), localPath = repo.id.toString()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRepo(repo: RepoEntity) {
        withContext(Dispatchers.IO) {
            workspace.migrateIfNeeded(repo, listOf(legacyReposDir))
            workspace.delete(repo)
            keys.delete(repo.id)
        }
        dao.delete(repo)
    }
}
