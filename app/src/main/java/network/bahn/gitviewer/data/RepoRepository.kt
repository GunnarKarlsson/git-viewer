package network.bahn.gitviewer.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class RepoRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).repoDao()
    private val keys = SshKeyStore(context)
    private val rootDir = File(context.filesDir, "repos").also { it.mkdirs() }

    fun getAllRepos(): Flow<List<RepoEntity>> = dao.getAll()

    suspend fun getRepo(id: Long) = dao.getById(id)

    fun hasSshKey(repoId: Long): Boolean = keys.hasKey(repoId)

    suspend fun sshPublicKey(repoId: Long): String? = withContext(Dispatchers.IO) {
        keys.publicKey(repoId)
    }

    /** Add a new repo (does NOT clone yet). */
    suspend fun addRepo(name: String, url: String, generateSshKey: Boolean = false): Long {
        val safeName = name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val local = File(rootDir, safeName).absolutePath
        val id = dao.insert(RepoEntity(name = name, url = url, localPath = local))
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
            val dir = File(repo.localPath)
            val identity = keys.privateKeyFile(repo.id).takeIf { it.exists() }
            GitHelper.cloneOrPull(repo.url, dir, identity)
            dao.update(repo.copy(lastPulled = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRepo(repo: RepoEntity) {
        withContext(Dispatchers.IO) {
            File(repo.localPath).deleteRecursively()
            keys.delete(repo.id)
        }
        dao.delete(repo)
    }
}
