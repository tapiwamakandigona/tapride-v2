package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.component.StatusBadge
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.ActiveRideViewModel
import io.tapiwa.tapride.util.toCurrencyString

/**
 * Active ride tracking screen.
 *
 * Shows the ride's current status, pickup/drop addresses, fare, and
 * a button to open the chat. Navigates to [RateRideScreen] on completion.
 *
 * @param navController Navigation controller.
 * @param rideId        Appwrite document ID of the active ride.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRideScreen(
    navController: NavHostController,
    rideId: String
) {
    val viewModel: ActiveRideViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ActiveRideViewModel(rideId) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate to rating screen when ride completes.
    LaunchedEffect(uiState.ride?.status) {
        val ride = uiState.ride
        if (ride?.status == "completed") {
            val rateeId = ride.driverId ?: ""
            navController.navigate(Route.RateRide.createRoute(rideId, rateeId)) {
                popUpTo(Route.ActiveRide.path) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_active_ride)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Route.Chat.createRoute(rideId))
                    }) {
                        Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.title_chat))
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            val ride = uiState.ride
            if (ride == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(padding)
                ) {
                    Text("Loading ride…")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusBadge(status = ride.status)

                    // Map placeholder
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("Live Map (osmdroid)")
                        }
                    }

                    Text(
                        text = "From: ${ride.pickupAddress}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "To: ${ride.dropAddress}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Fare: ${ride.fareEstimate.toCurrencyString()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
