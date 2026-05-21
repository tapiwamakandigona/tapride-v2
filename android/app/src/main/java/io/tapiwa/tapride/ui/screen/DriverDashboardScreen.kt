package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.component.RideCard
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.DriverViewModel

/**
 * Driver home screen.
 *
 * Shows an online/offline toggle, and — when online — lists open ride requests
 * that the driver can accept.
 *
 * @param navController Navigation controller.
 * @param viewModel     [DriverViewModel] instance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    navController: NavHostController,
    viewModel: DriverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate to active ride when driver has accepted one (only once per ride ID).
    val navigatedRideId = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.activeRide) {
        val ride = uiState.activeRide
        if (ride != null && navigatedRideId.value != ride.id) {
            navigatedRideId.value = ride.id
            navController.navigate(Route.ActiveRide.createRoute(ride.id)) {
                launchSingleTop = true
            }
        }
        if (ride == null) navigatedRideId.value = null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_driver_dashboard)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Route.Profile.path) }) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ── Online / Offline toggle ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isOnline) stringResource(R.string.label_online)
                           else stringResource(R.string.label_offline),
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = uiState.isOnline,
                    onCheckedChange = { viewModel.toggleOnline(context) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isOnline) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Go online to see ride requests.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (uiState.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.openRides.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "No open rides nearby.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.openRides, key = { it.id }) { ride ->
                        RideCard(
                            ride = ride,
                            onClick = { viewModel.acceptRide(ride) }
                        )
                    }
                }
            }
        }
    }
}
