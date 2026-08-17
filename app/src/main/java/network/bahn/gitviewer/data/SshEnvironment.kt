package network.bahn.gitviewer.data

import org.apache.sshd.common.util.io.PathUtils
import java.io.File

object SshEnvironment {
    fun install(filesDir: File) {
        val home = File(filesDir, "home").also { it.mkdirs() }
        File(home, ".ssh").mkdirs()
        System.setProperty("user.home", home.absolutePath)
        if (System.getProperty("user.name").isNullOrBlank()) {
            System.setProperty("user.name", "gitviewer")
        }
        PathUtils.setUserHomeFolderResolver { home.toPath() }
    }
}
