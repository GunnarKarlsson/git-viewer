package network.bahn.gitviewer.ui.viewer

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
import network.bahn.gitviewer.data.RepoWorkspace
import network.bahn.gitviewer.ui.theme.gitViewerTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    relativePath: String,
    repoId: Long,
    repository: RepoRepository,
    onBack: () -> Unit
) {
    val isMarkdown = relativePath.substringAfterLast('.').equals("md", ignoreCase = true) ||
            relativePath.substringAfterLast('.').equals("markdown", ignoreCase = true)

    var content by remember { mutableStateOf("Loading…") }
    var repoName by remember { mutableStateOf("") }

    LaunchedEffect(repoId, relativePath) {
        val repo = repository.getRepo(repoId) ?: return@LaunchedEffect
        repoName = repo.name
        content = try {
            repository.readFile(repo, relativePath)
        } catch (e: Exception) {
            "Error reading file:\n${e.message}"
        }
    }

    val breadcrumb = remember(repoName, relativePath) {
        RepoWorkspace.breadcrumb(repoName.ifEmpty { "…" }, relativePath)
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
