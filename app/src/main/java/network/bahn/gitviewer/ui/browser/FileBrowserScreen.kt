package network.bahn.gitviewer.ui.browser

// ============================================================
//  ui/browser/FileBrowserScreen.kt
// ============================================================

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
import network.bahn.gitviewer.data.RepoRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    repoId: Long,
    repository: RepoRepository,
    onFileClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var currentDir by remember { mutableStateOf<File?>(null) }
    var entries by remember { mutableStateOf<List<File>>(emptyList()) }
    var title by remember { mutableStateOf("…") }

    // Load root when repo is ready
    LaunchedEffect(repoId) {
        val repo = repository.getRepo(repoId) ?: return@LaunchedEffect
        val root = File(repo.localPath)
        if (!root.exists()) {
            title = "Not cloned yet – pull first"
            return@LaunchedEffect
        }
        currentDir = root
        title = repo.name
    }

    // Refresh listing whenever currentDir changes
    LaunchedEffect(currentDir) {
        val dir = currentDir ?: return@LaunchedEffect
        entries = dir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()
        title = dir.name.ifEmpty { "root" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = {
                        val parent = currentDir?.parentFile
                        val root = currentDir // we keep the original root in memory via repo
                        // simple: go up if not at repo root
                        if (parent != null && parent.absolutePath.startsWith(
                                // crude but works for our layout
                                currentDir!!.absolutePath.substringBeforeLast("/")
                            )
                        ) {
                            // better: keep a stack, but for minimalism we just go to parent
                            // (real apps should keep a path stack)
                            currentDir = parent
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (currentDir == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Repository not pulled yet")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(entries, key = { it.absolutePath }) { file ->
                    ListItem(
                        headlineContent = { Text(file.name) },
                        leadingContent = {
                            Icon(
                                if (file.isDirectory) Icons.Default.Folder
                                else Icons.Default.InsertDriveFile,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable {
                            if (file.isDirectory) {
                                currentDir = file
                            } else {
                                onFileClick(file.absolutePath)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}