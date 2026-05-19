package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.component.BottomNav
import io.tapiwa.tapride.ui.component.LoadingButton
import io.tapiwa.tapride.ui.component.RideCard
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.RiderViewModel
import io.tapiwa.tapride.util.toCurrencyString

/**
 * Rider home screen.
 *
 * Shows a map placeholder, pickup/destination inputs, fare estimate, and a
 * request-ride button. Redirects to [ActiveRideScreen] when a ride becomes active.
 *
 * @param navController Navigation controller.
 * @param viewModel     [RiderViewModel] instance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderDashboardScreen(
    navController: NavHostController,
    viewModel: RiderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate to active ride when one is found.
    LaunchedEffect(uiState.currentRide) {
        val ride = uiState.currentRide
        if (ride != null && ride.status in listOf("accepted", "in_progress")) {
            navController.navigate(Route.ActiveRide.createRoute(ride.id))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_rider_dashboard)) },
                actions = {
                    IconButton(onClick = { navController.navigate(Route.Profile.path) }) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BottomNav(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
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
            // ── Map placeholder ──────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Map View (osmdroid)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Pickup field ─────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.pickupAddress,
                onValueChange = { viewModel.setPickup(0.0, 0.0, it) },
                label = { Text(stringResource(R.string.label_pickup)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Destination field ─────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.dropAddress,
                onValueChange = { viewModel.setDrop(0.0, 0.0, it) },
                label = { Text(stringResource(R.string.label_destination)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (uiState.fareEstimate > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.label_fare_estimate)}: ${uiState.fareEstimate.toCurrencyString()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.currentRide == null) {
                LoadingButton(
                    text = stringResource(R.string.btn_request_ride),
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.requestRide() }
                )
            } else {
                // Ride is in "requested" state — show cancel option.
                Text(
                    text = "Waiting for a driver…",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.cancelRide() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_cancel_ride))
                }
            }
        }
    }
}
