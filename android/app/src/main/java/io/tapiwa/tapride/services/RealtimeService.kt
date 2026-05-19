package io.tapiwa.tapride.services

import io.appwrite.services.Realtime
import io.appwrite.models.RealtimeSubscription
import io.tapiwa.tapride.util.TapLogger

/**
 * Helper that wraps the raw Appwrite [Realtime] service, providing a
 * clean subscribe/unsubscribe API used by ViewModels.
 *
 * @param realtime The Appwrite [Realtime] service instance.
 */
class RealtimeService(private val realtime: Realtime) {

    /**
     * Subscribes to one or more Appwrite channels and invokes [onEvent]
     * for every incoming event payload.
     *
     * @param channels Vararg list of Appwrite channel strings,
     *   e.g. `"databases.tapride_db.collections.rides.documents"`.
     * @param onEvent Callback invoked with the raw event object on the main thread.
     * @return A [RealtimeSubscription] token — keep this reference to unsubscribe.
     */
    fun subscribe(
        vararg channels: String,
        onEvent: (Any) -> Unit
    ): RealtimeSubscription {
        TapLogger.d("RealtimeService", "Subscribing to: ${channels.toList()}")
        return realtime.subscribe(*channels) { event ->
            onEvent(event)
        }
    }

    /**
     * Cancels a previously created [RealtimeSubscription].
     *
     * @param subscription The subscription token returned by [subscribe].
     */
    fun unsubscribe(subscription: RealtimeSubscription) {
        TapLogger.d("RealtimeService", "Unsubscribing from realtime channel")
        subscription.close()
    }
}
