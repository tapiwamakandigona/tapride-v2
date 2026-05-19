package io.tapiwa.tapride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import io.tapiwa.tapride.ui.navigation.TapRideNavGraph
import io.tapiwa.tapride.ui.theme.TapRideTheme

/**
 * Single-Activity entry point for TapRide.
 *
 * Sets up the Compose content with [TapRideTheme] and boots the
 * [TapRideNavGraph] which handles all screen routing.
 *
 * The Appwrite [io.tapiwa.tapride.services.AppwriteClient] is already
 * initialised in [TapRideApplication.onCreate] before this Activity starts.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapRideTheme {
                val navController = rememberNavController()
                TapRideNavGraph(navController = navController)
            }
        }
    }
}
