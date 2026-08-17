package network.bahn.gitviewer

// ============================================================
//  MainActivity.kt
// ============================================================

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import network.bahn.gitviewer.data.RepoRepository
import network.bahn.gitviewer.ui.home.HomeScreen
import network.bahn.gitviewer.ui.browser.FileBrowserScreen
import network.bahn.gitviewer.ui.viewer.FileViewerScreen
import network.bahn.gitviewer.ui.add.AddRepoScreen
import network.bahn.gitviewer.ui.theme.GitViewerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = RepoRepository(applicationContext)

        setContent {
            GitViewerTheme {
                AppNav(repo)
            }
        }
    }
}

@Composable
fun AppNav(repo: RepoRepository) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                repository = repo,
                onAddClick = { nav.navigate("add") },
                onRepoClick = { id -> nav.navigate("browser/$id") }
            )
        }
        composable("add") {
            AddRepoScreen(
                repository = repo,
                onDone = { nav.popBackStack() }
            )
        }
        composable(
            "browser/{repoId}?dir={dir}",
            arguments = listOf(
                navArgument("repoId") { type = NavType.LongType },
                navArgument("dir") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStack ->
            val id = backStack.arguments!!.getLong("repoId")
            val relativePath = Uri.decode(backStack.arguments!!.getString("dir").orEmpty())
            FileBrowserScreen(
                repoId = id,
                relativePath = relativePath,
                repository = repo,
                onFileClick = { path ->
                    nav.navigate("viewer/$id/${path.replace("/", "|")}")
                },
                onDirClick = { dirPath ->
                    nav.navigate("browser/$id?dir=${Uri.encode(dirPath, "/")}")
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            "viewer/{repoId}/{path}",
            arguments = listOf(
                navArgument("repoId") { type = NavType.LongType },
                navArgument("path") { type = NavType.StringType }
            )
        ) { backStack ->
            val repoId = backStack.arguments!!.getLong("repoId")
            val path = backStack.arguments!!.getString("path")!!.replace("|", "/")
            FileViewerScreen(
                relativePath = path,
                repoId = repoId,
                repository = repo,
                onBack = { nav.popBackStack() }
            )
        }
    }
}