package network.bahn.gitviewer.ui.viewer

// ============================================================
//  ui/viewer/FileViewerScreen.kt
// ============================================================

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import network.bahn.gitviewer.data.RepoRepository
import network.bahn.gitviewer.ui.theme.gitViewerTopAppBarColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    path: String,
    repoId: Long,
    repository: RepoRepository,
    onBack: () -> Unit
) {
    val file = remember(path) { File(path) }
    val isMarkdown = file.extension.equals("md", ignoreCase = true) ||
            file.extension.equals("markdown", ignoreCase = true)

    var content by remember { mutableStateOf("Loading…") }
    var repoName by remember { mutableStateOf("") }
    var repoRoot by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(path) {
        content = try {
            file.readText()
        } catch (e: Exception) {
            "Error reading file:\n${e.message}"
        }
    }

    LaunchedEffect(repoId) {
        val repo = repository.getRepo(repoId) ?: return@LaunchedEffect
        repoName = repo.name
        repoRoot = File(repo.localPath)
    }

    val breadcrumb = remember(file, repoRoot, repoName) {
        breadcrumbFromRoot(file, repoRoot, repoName.ifEmpty { file.name })
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
        if (isMarkdown) {
            MarkdownText(
                markdown = content,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        } else {
            Text(
                text = content,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun breadcrumbFromRoot(file: File, repoRoot: File?, repoName: String): String {
    if (repoRoot == null) return repoName
    val relative = file.toRelativeString(repoRoot)
    if (relative.isEmpty() || relative == ".") return repoName
    val segments = relative.split(File.separatorChar).filter { it.isNotEmpty() }
    return (listOf(repoName) + segments).joinToString(" / ")
}
