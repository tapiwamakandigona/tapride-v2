package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import io.tapiwa.tapride.R
import io.tapiwa.tapride.ui.component.LoadingButton
import io.tapiwa.tapride.ui.navigation.Route
import io.tapiwa.tapride.ui.viewmodel.AuthViewModel

/**
 * Registration screen for new TapRide users.
 *
 * Collects name, phone, email, password and role. Driver-specific fields
 * (vehicle type, licence plate) are shown only when the driver role is selected.
 *
 * @param navController Navigation controller for routing after registration.
 * @param authViewModel Shared [AuthViewModel] instance.
 */
@Composable
fun RegisterScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("rider") }
    var vehicleType by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }

    // Navigate on successful registration.
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            val dest = if (profile.role == "driver") Route.DriverDashboard.path else Route.RiderDashboard.path
            navController.navigate(dest) {
                popUpTo(Route.Register.path) { inclusive = true }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(stringResource(R.string.label_full_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.label_phone)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.label_email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.label_password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Role selector
            Text(text = stringResource(R.string.label_role), style = MaterialTheme.typography.labelMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("rider", "driver").forEach { r ->
                    FilterChip(
                        selected = role == r,
                        onClick = { role = r },
                        label = { Text(r.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Driver-only fields
            if (role == "driver") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = { Text(stringResource(R.string.label_vehicle_type)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text(stringResource(R.string.label_license_plate)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoadingButton(
                text = stringResource(R.string.btn_register),
                isLoading = uiState.isLoading,
                onClick = {
                    authViewModel.register(
                        email = email,
                        password = password,
                        fullName = fullName,
                        phone = phone,
                        role = role,
                        vehicleType = vehicleType,
                        licensePlate = licensePlate
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = stringResource(R.string.link_have_account))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
