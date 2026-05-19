package io.tapiwa.tapride.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.navigation.Route

/** Represents a single bottom navigation destination. */
private data class NavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int
)

private val navItems = listOf(
    NavItem(Route.RiderDashboard.path, Icons.Default.Home, R.string.nav_home),
    NavItem(Route.RideHistory.path, Icons.Default.History, R.string.nav_history),
    NavItem(Route.Profile.path, Icons.Default.Person, R.string.nav_profile)
)

/**
 * Bottom navigation bar shared by rider-side screens.
 *
 * @param currentRoute The currently active route string.
 * @param onNavigate   Callback invoked with the target route when a tab is selected.
 */
@Composable
fun BottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = stringResource(item.labelRes)) },
                label = { Text(text = stringResource(item.labelRes)) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
