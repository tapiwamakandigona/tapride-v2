package io.tapiwa.tapride

import android.app.Application
import io.tapiwa.tapride.services.AppwriteClient

/**
 * Custom [Application] class for TapRide.
 *
 * Responsibilities:
 * - Initialises the [AppwriteClient] singleton so all repositories
 *   and ViewModels can use it immediately after app launch.
 */
class TapRideApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Bootstrap the Appwrite client with the application context.
        AppwriteClient.init(this)
    }
}
