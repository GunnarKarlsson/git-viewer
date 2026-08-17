// ============================================================
//  data/RepoRepository.kt
// ============================================================

package network.bahn.gitviewer.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class RepoRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).repoDao()
    private val rootDir = File(context.filesDir, "repos").also { it.mkdirs() }

    fun getAllRepos(): Flow<List<RepoEntity>> = dao.getAll()

    suspend fun getRepo(id: Long) = dao.getById(id)

    /** Add a new repo (does NOT clone yet). */
    suspend fun addRepo(name: String, url: String): Long {
        val safeName = name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val local = File(rootDir, safeName).absolutePath
        return dao.insert(RepoEntity(name = name, url = url, localPath = local))
    }

    /** Clone / pull the given repo. Updates lastPulled. */
    suspend fun pullRepo(repo: RepoEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dir = File(repo.localPath)
            GitHelper.cloneOrPull(repo.url, dir)
            dao.update(repo.copy(lastPulled = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRepo(repo: RepoEntity) {
        withContext(Dispatchers.IO) {
            File(repo.localPath).deleteRecursively()
        }
        dao.delete(repo)
    }
}