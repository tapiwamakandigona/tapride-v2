package io.tapiwa.tapride.services

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import io.tapiwa.tapride.constants.AppwriteConfig

/**
 * Singleton that owns all Appwrite service instances.
 *
 * Call [init] once in [io.tapiwa.tapride.TapRideApplication.onCreate].
 * All repositories and ViewModels obtain their service references from here.
 */
object AppwriteClient {

    private lateinit var _client: Client

    /** The underlying Appwrite [Client]. */
    val client: Client
        get() = _client

    /** Appwrite [Account] service — lazy so the client is ready first. */
    val account: Account by lazy { Account(_client) }

    /** Appwrite [Databases] service. */
    val databases: Databases by lazy { Databases(_client) }

    /** Appwrite [Realtime] service for live subscriptions. */
    val realtime: Realtime by lazy { Realtime(_client) }

    /**
     * Initialises the Appwrite [Client] with the app [Context].
     * Must be called before accessing any service property.
     *
     * @param context Application context used to configure the HTTP client.
     */
    fun init(context: Context) {
        _client = Client(context)
            .setEndpoint(AppwriteConfig.ENDPOINT)
            .setProject(AppwriteConfig.PROJECT_ID)
    }
}
