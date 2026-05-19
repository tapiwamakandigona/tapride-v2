package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.tapiwa.tapride.R
import io.tapiwa.tapride.data.model.Rating
import io.tapiwa.tapride.data.repository.AuthRepository
import io.tapiwa.tapride.data.repository.RatingRepository
import io.tapiwa.tapride.services.AppwriteClient
import io.tapiwa.tapride.ui.component.LoadingButton
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.util.currentIso8601
import kotlinx.coroutines.launch

/**
 * Post-ride rating screen.
 *
 * Collects a star rating (1–5) and optional comment, then submits via
 * [RatingRepository]. Navigates back to the appropriate dashboard on completion.
 *
 * @param navController Navigation controller.
 * @param rideId        Appwrite document ID of the completed ride.
 * @param rateeId       Appwrite user ID of the person being rated.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateRideScreen(
    navController: NavHostController,
    rideId: String,
    rateeId: String
) {
    val ratingRepo = remember { RatingRepository(AppwriteClient.databases) }
    val authRepo = remember { AuthRepository(AppwriteClient.account) }
    val scope = rememberCoroutineScope()

    var selectedStars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            errorMsg = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_rate_ride)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "How was your ride?",
                style = MaterialTheme.typography.headlineSmall
            )

            // ── Star selector ────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.Center) {
                (1..5).forEach { star ->
                    IconButton(onClick = { selectedStars = star }) {
                        Icon(
                            imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "$star stars",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(stringResource(R.string.hint_review)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            LoadingButton(
                text = stringResource(R.string.btn_submit_rating),
                isLoading = isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        authRepo.currentUser().onSuccess { user ->
                            ratingRepo.createRating(
                                Rating(
                                    id = "",
                                    rideId = rideId,
                                    raterId = user.id,
                                    rateeId = rateeId,
                                    stars = selectedStars,
                                    comment = comment,
                                    createdAt = currentIso8601()
                                )
                            ).onSuccess {
                                isLoading = false
                                navController.navigate(Route.RiderDashboard.path) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }.onFailure { e ->
                                isLoading = false
                                errorMsg = e.message
                            }
                        }.onFailure { e ->
                            isLoading = false
                            errorMsg = e.message
                        }
                    }
                }
            )
        }
    }
}
