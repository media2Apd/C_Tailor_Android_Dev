package com.cuso.mobile.view.composable

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg

// Reusable exit confirmation dialog. Renders only when show is true, so
// callers can drive it from a single back-press flag without extra guards.
@Composable
fun ExitAppDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = whiteBg,
        title = {
            Text(
                text = "Exit App",
                color = blackTitle,
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to exit?",
                color = blackTitle,
                fontSize = tokens.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = { (context as? Activity)?.finish() }) {
                Text("Exit", color = Color.Red, fontSize = tokens.bodyLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = blackTitle, fontSize = tokens.bodyLarge)
            }
        }
    )
}
