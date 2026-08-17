// ============================================================
//  ui/add/AddRepoScreen.kt
// ============================================================

package network.bahn.gitviewer.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import network.bahn.gitviewer.data.RepoRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepoScreen(
    repository: RepoRepository,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Repository") },
                navigationIcon = {
                    TextButton(onClick = onDone) { Text("Cancel") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                label = { Text("Git URL (https://…)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    if (name.isBlank() || url.isBlank()) return@Button
                    scope.launch {
                        saving = true
                        repository.addRepo(name.trim(), url.trim())
                        saving = false
                        onDone()
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