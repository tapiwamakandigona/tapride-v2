package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.AuthViewModel

/**
 * Full-screen splash shown on cold launch.
 *
 * Checks whether an active session exists and routes to:
 * - Rider Dashboard if `profile.role == "rider"`
 * - Driver Dashboard if `profile.role == "driver"`
 * - Login screen if no session is found
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    // Trigger the session check once on composition.
    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    // React to auth state changes and navigate away from splash.
    LaunchedEffect(uiState.profile, uiState.isLoading) {
        if (!uiState.isLoading) {
            val destination = when (uiState.profile?.role) {
                "driver" -> Route.DriverDashboard.path
                "rider"  -> Route.RiderDashboard.path
                else     -> Route.Login.path
            }
            navController.navigate(destination) {
                popUpTo(Route.Splash.path) { inclusive = true }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your ride, on demand.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
