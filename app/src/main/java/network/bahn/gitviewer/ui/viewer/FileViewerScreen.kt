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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    path: String,
    onBack: () -> Unit
) {
    val file = remember(path) { File(path) }
    val isMarkdown = file.extension.equals("md", ignoreCase = true) ||
            file.extension.equals("markdown", ignoreCase = true)

    var content by remember { mutableStateOf("Loading…") }

    LaunchedEffect(path) {
        content = try {
            file.readText()
        } catch (e: Exception) {
            "Error reading file:\n${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
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