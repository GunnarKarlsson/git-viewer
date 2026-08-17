package network.bahn.gitviewer

// ============================================================
//  MainActivity.kt
// ============================================================

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
            "browser/{repoId}",
            arguments = listOf(navArgument("repoId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments!!.getLong("repoId")
            FileBrowserScreen(
                repoId = id,
                repository = repo,
                onFileClick = { path ->
                    nav.navigate("viewer/${path.replace("/", "%2F")}")
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(
            "viewer/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStack ->
            val encoded = backStack.arguments!!.getString("path")!!
            val path = encoded.replace("%2F", "/")
            FileViewerScreen(path = path, onBack = { nav.popBackStack() })
        }
    }
}