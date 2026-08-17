// ============================================================
//  ui/home/HomeScreen.kt
// ============================================================

package network.bahn.gitviewer.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import network.bahn.gitviewer.data.RepoEntity
import network.bahn.gitviewer.data.RepoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: RepoRepository,
    onAddClick: () -> Unit,
    onRepoClick: (Long) -> Unit
) {
    val repos by repository.getAllRepos().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var pullingId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var repoToDelete by remember { mutableStateOf<RepoEntity?>(null) }
    var sshKeyRepo by remember { mutableStateOf<RepoEntity?>(null) }
    var sshPublicKey by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Git Viewer") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add repo")
            }
        }
    ) { padding ->
        if (repos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No repositories yet.\nTap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(repos, key = { it.id }) { repo ->
                    RepoCard(
                        repo = repo,
                        isPulling = pullingId == repo.id,
                        hasSshKey = repository.hasSshKey(repo.id),
                        onClick = { onRepoClick(repo.id) },
                        onShowKey = {
                            scope.launch {
                                sshKeyRepo = repo
                                sshPublicKey = repository.sshPublicKey(repo.id)
                            }
                        },
                        onPull = {
                            scope.launch {
                                pullingId = repo.id
                                error = null
                                repository.pullRepo(repo)
                                    .onFailure { error = it.message }
                                pullingId = null
                            }
                        },
                        onDelete = { repoToDelete = repo }
                    )
                }
            }
        }

        error?.let {
            AlertDialog(
                onDismissRequest = { error = null },
                title = { Text("Pull failed") },
                text = { Text(it) },
                confirmButton = {
                    TextButton(onClick = { error = null }) { Text("OK") }
                }
            )
        }

        sshKeyRepo?.let {
            val clipboard = LocalClipboardManager.current
            AlertDialog(
                onDismissRequest = {
                    sshKeyRepo = null
                    sshPublicKey = null
                },
                title = { Text("SSH public key") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add this to GitHub as a deploy key or account SSH key.")
                        SelectionContainer {
                            Text(
                                sshPublicKey ?: "No key found",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            sshPublicKey?.let { clipboard.setText(AnnotatedString(it)) }
                        },
                        enabled = sshPublicKey != null
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        sshKeyRepo = null
                        sshPublicKey = null
                    }) { Text("Close") }
                }
            )
        }

        repoToDelete?.let { repo ->
            AlertDialog(
                onDismissRequest = { repoToDelete = null },
                title = { Text("Delete repository?") },
                text = { Text("Delete ${repo.name}? The local clone will be removed.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            repoToDelete = null
                            scope.launch { repository.deleteRepo(repo) }
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { repoToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun RepoCard(
    repo: RepoEntity,
    isPulling: Boolean,
    hasSshKey: Boolean,
    onClick: () -> Unit,
    onShowKey: () -> Unit,
    onPull: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(repo.name, style = MaterialTheme.typography.titleMedium)
                Text(repo.url, style = MaterialTheme.typography.bodySmall)
                if (repo.lastPulled > 0) {
                    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    Text("Last pulled: ${fmt.format(Date(repo.lastPulled))}",
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            if (hasSshKey) {
                IconButton(onClick = onShowKey) {
                    Icon(Icons.Default.Key, "SSH public key")
                }
            }
            IconButton(onClick = onPull, enabled = !isPulling) {
                if (isPulling) CircularProgressIndicator(Modifier.size(24.dp))
                else Icon(Icons.Default.CloudDownload, "Pull")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete")
            }
        }
    }
}