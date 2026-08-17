// ============================================================
//  data/RepoEntity.kt
// ============================================================

package network.bahn.gitviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repos")
data class RepoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val localPath: String,          // relative workspace key: repo id
    val lastPulled: Long = 0L       // epoch millis
)