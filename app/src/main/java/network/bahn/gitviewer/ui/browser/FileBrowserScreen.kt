package network.bahn.gitviewer.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import network.bahn.gitviewer.data.RepoFsEntry
import network.bahn.gitviewer.data.RepoRepository
import network.bahn.gitviewer.data.RepoWorkspace
import network.bahn.gitviewer.ui.theme.gitViewerTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    repoId: Long,
    relativePath: String,
    repository: RepoRepository,
    onFileClick: (String) -> Unit,
    onDirClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var repoName by remember { mutableStateOf("…") }
    var cloned by remember { mutableStateOf(true) }
    var entries by remember { mutableStateOf<List<RepoFsEntry>>(emptyList()) }

    LaunchedEffect(repoId, relativePath) {
        val repo = repository.getRepo(repoId) ?: return@LaunchedEffect
        repoName = repo.name
        cloned = repository.isCloned(repo)
        entries = if (cloned) repository.listEntries(repo, relativePath) else emptyList()
    }

    val breadcrumb = remember(repoName, relativePath) {
        RepoWorkspace.breadcrumb(repoName, relativePath)
    }

    Scaffold(
        topBar = {
            Surface(
                color = gitViewerTopAppBarColors().containerColor,
                contentColor = gitViewerTopAppBarColors().titleContentColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                        .heightIn(min = 64.dp)
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                    Text(
                        text = breadcrumb,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    ) { padding ->
        if (!cloned) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Repository not pulled yet")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(entries, key = { it.relativePath }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.name) },
                        leadingContent = {
                            Icon(
                                if (entry.isDirectory) Icons.Default.Folder
                                else Icons.Default.InsertDriveFile,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable {
                            if (entry.isDirectory) onDirClick(entry.relativePath)
                            else onFileClick(entry.relativePath)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
