package io.tapiwa.tapride.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.tapiwa.tapride.ui.screen.*
import io.tapiwa.tapride.ui.viewmodel.AuthViewModel

/**
 * Sealed hierarchy of all navigation routes in TapRide.
 *
 * Using a sealed class ensures exhaustive `when` expressions and
 * prevents typos in route strings.
 */
sealed class Route(val path: String) {
    /** Full-screen splash shown while checking auth state. */
    object Splash : Route("splash")

    /** Email/password login screen. */
    object Login : Route("login")

    /** New user registration screen. */
    object Register : Route("register")

    /** Rider home screen with map + ride request. */
    object RiderDashboard : Route("rider_dashboard")

    /** Driver home screen with available-ride list. */
    object DriverDashboard : Route("driver_dashboard")

    /** In-ride tracking screen shared by rider and driver. */
    object ActiveRide : Route("active_ride/{rideId}") {
        /** Creates the full path with [rideId] substituted. */
        fun createRoute(rideId: String) = "active_ride/$rideId"
    }

    /** In-ride chat screen. */
    object Chat : Route("chat/{rideId}") {
        /** Creates the full path with [rideId] substituted. */
        fun createRoute(rideId: String) = "chat/$rideId"
    }

    /** Post-ride rating screen. */
    object RateRide : Route("rate_ride/{rideId}/{rateeId}") {
        /** Creates the full path with [rideId] and [rateeId] substituted. */
        fun createRoute(rideId: String, rateeId: String) = "rate_ride/$rideId/$rateeId"
    }

    /** Historical ride list. */
    object RideHistory : Route("ride_history")

    /** User profile screen. */
    object Profile : Route("profile")
}

/**
 * Root [NavHost] that wires all [Route] destinations to their Composable screens.
 *
 * @param navController The [NavHostController] driving navigation.
 */
@Composable
fun TapRideNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.path
    ) {
        composable(Route.Splash.path) {
            SplashScreen(navController = navController)
        }

        composable(Route.Login.path) {
            LoginScreen(navController = navController)
        }

        composable(Route.Register.path) {
            RegisterScreen(navController = navController)
        }

        composable(Route.RiderDashboard.path) {
            RiderDashboardScreen(navController = navController)
        }

        composable(Route.DriverDashboard.path) {
            DriverDashboardScreen(navController = navController)
        }

        composable(
            route = Route.ActiveRide.path,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ) { backStack ->
            val rideId = backStack.arguments?.getString("rideId").orEmpty()
            if (rideId.isNotEmpty()) {
                ActiveRideScreen(navController = navController, rideId = rideId)
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = Route.Chat.path,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ) { backStack ->
            val rideId = backStack.arguments?.getString("rideId") ?: ""
            ChatScreen(navController = navController, rideId = rideId)
        }

        composable(
            route = Route.RateRide.path,
            arguments = listOf(
                navArgument("rideId") { type = NavType.StringType },
                navArgument("rateeId") { type = NavType.StringType }
            )
        ) { backStack ->
            val rideId  = backStack.arguments?.getString("rideId").orEmpty()
            val rateeId = backStack.arguments?.getString("rateeId").orEmpty()
            if (rideId.isNotEmpty()) {
                RateRideScreen(navController = navController, rideId = rideId, rateeId = rateeId)
            } else {
                navController.popBackStack()
            }
        }

        composable(Route.RideHistory.path) {
            RideHistoryScreen(navController = navController)
        }

        composable(Route.Profile.path) {
            ProfileScreen(navController = navController)
        }
    }
}
