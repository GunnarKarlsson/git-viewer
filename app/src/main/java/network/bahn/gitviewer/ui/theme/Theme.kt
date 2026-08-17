package network.bahn.gitviewer.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PosterBlue = Color(0xFF072A4A)
val PosterTeal = Color(0xFF0E8A86)
val PosterOrange = Color(0xFFFF7A18)

private val GitViewerColorScheme = lightColorScheme(
    primary = PosterOrange,
    onPrimary = Color.White,
    secondary = PosterTeal,
    onSecondary = Color.White,
    tertiary = PosterBlue,
    onTertiary = Color.White
)

@Composable
fun GitViewerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GitViewerColorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gitViewerTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
)
