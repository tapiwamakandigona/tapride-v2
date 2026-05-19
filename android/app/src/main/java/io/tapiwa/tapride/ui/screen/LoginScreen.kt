package io.tapiwa.tapride.ui.screen

import androidx.compose.foundation.layout.*
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
 * Email/password login screen.
 *
 * On successful login, navigates to the appropriate dashboard based on the
 * user's role stored in their [io.tapiwa.tapride.data.model.Profile].
 *
 * @param navController Navigation controller for routing.
 * @param authViewModel Shared [AuthViewModel] instance.
 */
@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Navigate on successful login.
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            val dest = if (profile.role == "driver") Route.DriverDashboard.path else Route.RiderDashboard.path
            navController.navigate(dest) {
                popUpTo(Route.Login.path) { inclusive = true }
            }
        }
    }

    // Show snackbar on error.
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.label_email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.label_password)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoadingButton(
                text = stringResource(R.string.btn_login),
                isLoading = uiState.isLoading,
                onClick = { authViewModel.login(email, password) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate(Route.Register.path)
            }) {
                Text(text = stringResource(R.string.link_no_account))
            }
        }
    }
}
