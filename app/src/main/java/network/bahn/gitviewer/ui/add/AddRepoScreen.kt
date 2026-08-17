package network.bahn.gitviewer.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import network.bahn.gitviewer.data.RepoRepository
import network.bahn.gitviewer.ui.theme.gitViewerTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepoScreen(
    repository: RepoRepository,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var useSshKey by remember { mutableStateOf(true) }
    var publicKey by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (publicKey != null) "SSH public key" else "Add Repository") },
                navigationIcon = {
                    TextButton(
                        onClick = onDone,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(if (publicKey != null) "Done" else "Cancel")
                    }
                },
                colors = gitViewerTopAppBarColors()
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (publicKey != null) {
                Text(
                    "Add this public key to GitHub before pulling. " +
                        "Repo → Settings → Deploy keys, or your account SSH keys.",
                    style = MaterialTheme.typography.bodyMedium
                )
                SelectionContainer {
                    Text(publicKey!!, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = { clipboard.setText(AnnotatedString(publicKey!!)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy public key")
                }
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("Done")
                }
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Git URL") },
                    placeholder = { Text("https://github.com/… or git@github.com:…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useSshKey, onCheckedChange = { useSshKey = it })
                    Text("Generate SSH key for private GitHub repos")
                }
                if (useSshKey) {
                    Text(
                        "An Ed25519 key pair is created on this device. " +
                            "You’ll copy the public key to GitHub; the private key never leaves the app.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = {
                        if (name.isBlank() || url.isBlank()) return@Button
                        scope.launch {
                            saving = true
                            error = null
                            try {
                                val id = repository.addRepo(
                                    name.trim(),
                                    url.trim(),
                                    generateSshKey = useSshKey
                                )
                                if (useSshKey) {
                                    publicKey = repository.sshPublicKey(id)
                                        ?: error("Key was not generated")
                                } else {
                                    onDone()
                                }
                            } catch (e: Exception) {
                                error = e.message ?: "Failed to save repository"
                            }
                            saving = false
                        }
                    },
                    enabled = !saving && name.isNotBlank() && url.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (saving) CircularProgressIndicator(Modifier.size(20.dp))
                    else Text("Save")
                }
            }
        }
    }
}
