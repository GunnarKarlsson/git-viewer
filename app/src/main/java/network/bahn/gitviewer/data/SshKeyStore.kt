package network.bahn.gitviewer.data

import android.content.Context
import org.apache.sshd.common.config.keys.KeyUtils
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.Security

class SshKeyStore(context: Context) {

    private val root = File(context.filesDir, "ssh").also { it.mkdirs() }

    init {
        ensureBouncyCastle()
    }

    fun hasKey(repoId: Long): Boolean = privateKeyFile(repoId).exists()

    fun privateKeyFile(repoId: Long): File = File(dir(repoId), "id_ed25519")

    fun publicKey(repoId: Long): String? {
        val file = File(dir(repoId), "id_ed25519.pub")
        return file.takeIf { it.exists() }?.readText()?.trim()
    }

    fun generate(repoId: Long): String {
        val dir = dir(repoId).also { it.mkdirs() }
        val privateFile = File(dir, "id_ed25519")
        val publicFile = File(dir, "id_ed25519.pub")

        val keyPair = try {
            KeyUtils.generateKeyPair("ssh-ed25519", 256)
        } catch (_: Exception) {
            KeyUtils.generateKeyPair("ssh-rsa", 4096)
        }

        privateFile.outputStream().use { out ->
            OpenSSHKeyPairResourceWriter.INSTANCE.writePrivateKey(
                keyPair,
                COMMENT,
                null,
                out
            )
        }
        publicFile.outputStream().use { out ->
            OpenSSHKeyPairResourceWriter.INSTANCE.writePublicKey(keyPair, COMMENT, out)
        }
        restrict(privateFile)
        return publicFile.readText().trim()
    }

    fun delete(repoId: Long) {
        dir(repoId).deleteRecursively()
    }

    private fun dir(repoId: Long) = File(root, repoId.toString())

    private fun restrict(file: File) {
        file.setReadable(false, false)
        file.setWritable(false, false)
        file.setReadable(true, true)
        file.setWritable(true, true)
    }

    private companion object {
        const val COMMENT = "gitviewer"

        fun ensureBouncyCastle() {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            }
            Security.addProvider(BouncyCastleProvider())
        }
    }
}
