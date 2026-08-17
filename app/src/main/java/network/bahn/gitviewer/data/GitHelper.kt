package network.bahn.gitviewer.data

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.sshd.JGitKeyCache
import org.eclipse.jgit.transport.sshd.ServerKeyDatabase
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder
import java.io.File
import java.net.InetSocketAddress
import java.security.PublicKey

object GitHelper {

    /** Clone or pull. Returns the local folder or throws. */
    fun cloneOrPull(url: String, targetDir: File, identityFile: File? = null): File {
        val remoteUrl = if (identityFile != null) sshUrl(url) else url
        val transport = identityFile?.let { sshTransport(it) }

        if (targetDir.exists() && File(targetDir, ".git").exists()) {
            Git.open(targetDir).use { git ->
                val pull = git.pull()
                if (transport != null) pull.setTransportConfigCallback(transport)
                pull.call()
            }
        } else {
            targetDir.mkdirs()
            val clone = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(targetDir)
            if (transport != null) clone.setTransportConfigCallback(transport)
            clone.call().close()
        }
        return targetDir
    }

    private fun sshUrl(url: String): String {
        val https = Regex(
            """^https://github\.com/([^/]+)/([^/]+?)(?:\.git)?/?$""",
            RegexOption.IGNORE_CASE
        )
        val match = https.matchEntire(url.trim())
        return if (match != null) {
            "git@github.com:${match.groupValues[1]}/${match.groupValues[2]}.git"
        } else {
            url.trim()
        }
    }

    private fun sshTransport(identityFile: File): TransportConfigCallback {
        val sshDir = identityFile.parentFile
        val factory = SshdSessionFactoryBuilder()
            .setPreferredAuthentications("publickey")
            .setHomeDirectory(sshDir)
            .setSshDirectory(sshDir)
            .setDefaultIdentities { _: File -> listOf(identityFile.toPath()) }
            .setConfigStoreFactory { _, _, _ -> null }
            .setServerKeyDatabase { _, _ -> AcceptAllServerKeys }
            .build(JGitKeyCache())
        return TransportConfigCallback { transport ->
            if (transport is SshTransport) {
                transport.sshSessionFactory = factory
            }
        }
    }

    private object AcceptAllServerKeys : ServerKeyDatabase {
        override fun lookup(
            connectAddress: String,
            remoteAddress: InetSocketAddress,
            config: ServerKeyDatabase.Configuration
        ): List<PublicKey> = emptyList()

        override fun accept(
            connectAddress: String,
            remoteAddress: InetSocketAddress,
            serverKey: PublicKey,
            config: ServerKeyDatabase.Configuration,
            provider: org.eclipse.jgit.transport.CredentialsProvider?
        ): Boolean = true
    }
}
