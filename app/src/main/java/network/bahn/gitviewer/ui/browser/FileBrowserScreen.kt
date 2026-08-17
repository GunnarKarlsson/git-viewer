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
    relativePath: String,
    repository: RepoRepository,
    onFileClick: (String) -> Unit,
    onDirClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var repoRoot by remember { mutableStateOf<File?>(null) }
    var repoName by remember { mutableStateOf("…") }
    var entries by remember { mutableStateOf<List<File>>(emptyList()) }

    val currentDir = remember(repoRoot, relativePath) {
        repoRoot?.let { resolveInRepo(it, relativePath) }
    }

    LaunchedEffect(repoId) {
        val repo = repository.getRepo(repoId) ?: return@LaunchedEffect
        val root = File(repo.localPath)
        if (!root.exists()) {
            repoName = "Not cloned yet – pull first"
            return@LaunchedEffect
        }
        repoRoot = root
        repoName = repo.name
    }

    LaunchedEffect(currentDir) {
        val dir = currentDir ?: return@LaunchedEffect
        entries = dir.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()
    }

    val breadcrumb = remember(currentDir, repoRoot, repoName) {
        breadcrumbFromRoot(currentDir, repoRoot, repoName)
    }

    Scaffold(
        topBar = {
            Surface(color = TopAppBarDefaults.topAppBarColors().containerColor) {
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
                                onDirClick(file.toRelativeString(repoRoot!!))
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

private fun resolveInRepo(root: File, relativePath: String): File {
    if (relativePath.isEmpty() || relativePath == ".") return root
    val resolved = File(root, relativePath).canonicalFile
    val rootCanonical = root.canonicalFile
    return if (resolved.path.startsWith(rootCanonical.path)) resolved else root
}

private fun breadcrumbFromRoot(currentDir: File?, repoRoot: File?, repoName: String): String {
    if (currentDir == null || repoRoot == null) return repoName
    val relative = currentDir.toRelativeString(repoRoot)
    if (relative.isEmpty() || relative == ".") return repoName
    val segments = relative.split(File.separatorChar).filter { it.isNotEmpty() }
    return (listOf(repoName) + segments).joinToString(" / ")
}
