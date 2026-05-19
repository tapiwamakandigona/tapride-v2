package io.tapiwa.tapride.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.tapiwa.tapride.data.model.Ride
import io.tapiwa.tapride.util.toCurrencyString
import io.tapiwa.tapride.util.toDistanceString
import io.tapiwa.tapride.util.toReadableDateTime

/**
 * A card summarising a single [Ride].
 *
 * Used in both the Ride History list and the available-rides list shown to drivers.
 *
 * @param ride     The [Ride] to display.
 * @param onClick  Invoked when the card is tapped.
 * @param modifier Optional modifier.
 */
@Composable
fun RideCard(
    ride: Ride,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusBadge(status = ride.status)
                Text(
                    text = ride.requestedAt.toReadableDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pickup row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ride.pickupAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Drop row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ride.dropAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ride.distanceKm.toDistanceString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ride.fareEstimate.toCurrencyString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
