package network.bahn.gitviewer.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import network.bahn.gitviewer.R

val PosterBlue = Color(0xFF1B7BA3)
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

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val imageModifier = modifier
        .padding(start = 8.dp)
        .size(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Image(
        painter = painterResource(R.drawable.ic_app_logo),
        contentDescription = if (onClick != null) "Home" else null,
        modifier = imageModifier,
        contentScale = ContentScale.Fit
    )
}
