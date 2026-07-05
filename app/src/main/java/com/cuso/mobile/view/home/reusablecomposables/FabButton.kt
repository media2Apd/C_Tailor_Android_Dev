package com.cuso.mobile.view.home.reusablecomposables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Common config for the fixed bottom-end FAB button used across
 * Branch / Department / Designation / Lead / Customer / Measurements /
 * SalesOrder screens.
 */
data class FabConfig(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val containerColor: Color = Color(0xFF3B3BF9),
    val endPadding: androidx.compose.ui.unit.Dp = 10.dp,
    val bottomPadding: androidx.compose.ui.unit.Dp = 50.dp
)

/**
 * Wraps screen content in a Box so a fixed FAB (and optional SnackbarHost)
 * can float above it — same pattern used in Branch/Department/Designation/
 * Lead/Customer/Measurements/SalesOrder screens.
 *
 * Usage:
 * FabScaffold(
 *     fab = FabConfig("Add Branch", Icons.Default.Add) { showAddDialog = true },
 *     snackbarHostState = snackbarHostState   // optional
 * ) {
 *     // your existing screen Column goes here
 * }
 */
@Composable
fun FabScaffold(
    fab: FabConfig?,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (snackbarHostState != null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        if (fab != null) {
            Button(
                onClick = fab.onClick,
                colors = ButtonDefaults.buttonColors(containerColor = fab.containerColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = fab.endPadding, bottom = fab.bottomPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fab.label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(fab.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}