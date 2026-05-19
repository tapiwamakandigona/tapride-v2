package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
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
import io.tapiwa.tapride.ui.component.LoadingButton
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.AuthViewModel
import io.tapiwa.tapride.ui.viewmodel.ProfileViewModel

/**
 * User profile screen.
 *
 * Allows the user to view and edit their name, phone, and (if a driver)
 * vehicle details. Also provides a logout button.
 *
 * @param navController Navigation controller.
 * @param profileViewModel ViewModel managing profile data.
 * @param authViewModel    ViewModel managing auth state (for logout).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Prefill editable fields once profile loads.
    var fullName by remember(profileState.profile) {
        mutableStateOf(profileState.profile?.fullName ?: "")
    }
    var phone by remember(profileState.profile) {
        mutableStateOf(profileState.profile?.phone ?: "")
    }
    var vehicleType by remember(profileState.profile) {
        mutableStateOf(profileState.profile?.vehicleType ?: "")
    }
    var licensePlate by remember(profileState.profile) {
        mutableStateOf(profileState.profile?.licensePlate ?: "")
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(profileState.error) {
        profileState.error?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearError()
        }
    }
    LaunchedEffect(profileState.isSaved) {
        if (profileState.isSaved) {
            snackbarHostState.showSnackbar("Profile saved!")
            profileViewModel.consumeSavedEvent()
        }
    }

    // Navigate to login after logout.
    LaunchedEffect(authState.isLoggedOut) {
        if (authState.isLoggedOut) {
            navController.navigate(Route.Login.path) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_profile)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (profileState.isLoading && profileState.profile == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                profileState.profile?.let { profile ->
                    Text(
                        text = "Role: ${profile.role.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rating: ⭐ ${"%.1f".format(profile.rating)} (${profile.totalRides} rides)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider()
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(stringResource(R.string.label_full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.label_phone)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (profileState.profile?.role == "driver") {
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = { vehicleType = it },
                        label = { Text(stringResource(R.string.label_vehicle_type)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text(stringResource(R.string.label_license_plate)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                LoadingButton(
                    text = "Save Changes",
                    isLoading = profileState.isLoading,
                    onClick = {
                        profileViewModel.saveProfile(
                            fullName = fullName,
                            phone = phone,
                            vehicleType = vehicleType,
                            licensePlate = licensePlate
                        )
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_logout))
                }
            }
        }
    }
}
