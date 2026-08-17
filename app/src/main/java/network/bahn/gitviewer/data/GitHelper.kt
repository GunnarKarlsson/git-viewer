// ============================================================
//  data/GitHelper.kt  – thin JGit wrapper
// ============================================================

package network.bahn.gitviewer.data

import org.eclipse.jgit.api.Git
import java.io.File

object GitHelper {

    /** Clone or pull. Returns the local folder or throws. */
    fun cloneOrPull(url: String, targetDir: File): File {
        if (targetDir.exists() && File(targetDir, ".git").exists()) {
            // already cloned → pull
            Git.open(targetDir).use { git ->
                git.pull().call()
            }
        } else {
            // fresh clone
            targetDir.mkdirs()
            Git.cloneRepository()
                .setURI(url)
                .setDirectory(targetDir)
                .call()
                .close()
        }
        return targetDir
    }
}