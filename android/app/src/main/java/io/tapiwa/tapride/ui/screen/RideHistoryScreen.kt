package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.component.RideCard
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.RiderViewModel

/**
 * Ride history screen.
 *
 * Displays a scrollable list of all rides (completed, cancelled, etc.) for the
 * current rider. Tapping a card navigates to [ActiveRideScreen] if the ride is
 * still active, or shows a detail view otherwise.
 *
 * @param navController Navigation controller.
 * @param viewModel     Shared [RiderViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(
    navController: NavHostController,
    viewModel: RiderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadRideHistory()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_ride_history)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.rideHistory.isEmpty() -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    Text(
                        text = "No rides yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(uiState.rideHistory, key = { it.id }) { ride ->
                        RideCard(
                            ride = ride,
                            onClick = {
                                if (ride.status in listOf("accepted", "in_progress")) {
                                    navController.navigate(Route.ActiveRide.createRoute(ride.id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
