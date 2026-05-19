package io.tapiwa.tapride.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.tapiwa.tapride.data.model.Message
import io.tapiwa.tapride.util.toReadableDateTime

/**
 * A single chat message bubble.
 *
 * Outgoing messages (sent by [currentUserId]) appear on the right in the
 * primary colour; incoming messages appear on the left with a surface colour.
 *
 * @param message       The [Message] to render.
 * @param currentUserId The logged-in user's ID used to determine bubble alignment.
 * @param modifier      Optional modifier.
 */
@Composable
fun ChatBubble(
    message: Message,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    val isOwn = message.senderId == currentUserId
    val alignment = if (isOwn) Alignment.End else Alignment.Start
    val bgColor = if (isOwn)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOwn)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwn) 16.dp else 4.dp,
                        bottomEnd = if (isOwn) 4.dp else 16.dp
                    )
                )
                .background(bgColor)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = message.sentAt.toReadableDateTime(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
