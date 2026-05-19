package io.tapiwa.tapride.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A full-width [Button] that shows a [CircularProgressIndicator] while [isLoading]
 * is true, and disables itself to prevent double-taps.
 *
 * @param text      Label shown when not loading.
 * @param isLoading Whether to show the spinner instead of the label.
 * @param onClick   Invoked when the button is tapped.
 * @param modifier  Optional modifier applied to the button.
 */
@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.height(24.dp)
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
