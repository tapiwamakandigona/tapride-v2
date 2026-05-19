package io.tapiwa.tapride.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.tapiwa.tapride.ui.theme.*

/**
 * A small coloured pill that reflects a ride's status string.
 *
 * Colour mapping:
 * - `requested`   → amber
 * - `accepted`    → blue
 * - `in_progress` → green
 * - `completed`   → grey
 * - `cancelled`   → red
 *
 * @param status   The ride status string.
 * @param modifier Optional modifier.
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, label) = when (status) {
        "requested"   -> StatusRequested  to "Requested"
        "accepted"    -> StatusAccepted   to "Accepted"
        "in_progress" -> StatusInProgress to "In Progress"
        "completed"   -> StatusCompleted  to "Completed"
        "cancelled"   -> StatusCancelled  to "Cancelled"
        else          -> Color.Gray       to status.replaceFirstChar { it.uppercase() }
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = White,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
