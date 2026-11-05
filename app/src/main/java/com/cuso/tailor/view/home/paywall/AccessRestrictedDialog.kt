package com.cuso.tailor.view.home.paywall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

// Dark tint drawn over the blurred screen behind the dialog
private val ArOverlay = Color(0xFF111827).copy(alpha = 0.5f)

// Brand indigo used for the lock icon and the primary button
private val ArIndigo = Color(0xFF3B3BE8)

// Light indigo used for the icon circle and the permission chip
private val ArIndigoSoft = Color(0xFFEEEBFF)

// Text color of the permission chip
private val ArChipText = Color(0xFF4338CA)

// Title and body text colors
private val ArTitleText = Color(0xFF1F2937)
private val ArBodyText = Color(0xFF4B5563)

// Close icon, secondary button border and secondary button text colors
private val ArCloseIcon = Color(0xFF6B7280)
private val ArBorder = Color(0xFFD1D5DB)
private val ArSecondaryText = Color(0xFF374151)

/**
 * "Access Restricted" dialog.
 *
 * Shows a centered card with a lock icon, a message, the required permission chip
 * and two actions (Request Access / Go Back) on top of a dark scrim.
 *
 * To get the blurred background, blur the screen content that sits behind this dialog
 * while it is visible (see the usage example below).
 *
 * @param onRequestAccess called when the user taps "Request Access"
 * @param onGoBack called when the user taps "Go Back"
 * @param onDismiss called when the dialog is closed with the X button or by tapping outside
 * @param requiredPermission permission name shown in the chip
 * @param message explanation text shown under the title
 */
@Composable
fun AccessRestrictedDialog(
    onRequestAccess: () -> Unit,
    onGoBack: () -> Unit,
    onDismiss: () -> Unit = onGoBack,
    requiredPermission: String = "System Administration",
    message: String = "You don't have permission to view or change these settings. " +
            "Please contact your administrator to get access."
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            // Let our own scrim cover the whole screen
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Remove the default window dim because the dialog draws its own scrim
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        }

        // Entrance animation state: starts hidden and switches to visible on first frame
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ArOverlay)
                // Tapping on the scrim closes the dialog
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(250)) +
                        scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(450, easing = FastOutSlowInEasing)
                        ) +
                        slideInVertically(
                            animationSpec = tween(450, easing = FastOutSlowInEasing),
                            initialOffsetY = { it / 20 }
                        )
            ) {
                AccessRestrictedCard(
                    requiredPermission = requiredPermission,
                    message = message,
                    onGoBack = onGoBack,
                    onClose = onDismiss
                )
            }
        }
    }
}

@Composable
private fun AccessRestrictedCard(
    requiredPermission: String,
    message: String,
    onGoBack: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // Consume taps so they do not reach the scrim and close the dialog
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 24.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lock icon inside a soft indigo circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(ArIndigoSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = ArIndigo,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Access Restricted",
                    color = ArTitleText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Explanation message
                Text(
                    text = message,
                    color = ArBodyText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Required permission chip
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ArIndigoSoft)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Required: $requiredPermission",
                        color = ArChipText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Secondary action
                    OutlinedButton(
                        onClick = onGoBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ArBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = ArSecondaryText
                        )
                    ) {
                        Text(
                            text = "Go Back",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Close button in the top-right corner (44dp touch target)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = ArCloseIcon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/*
 * ─────────────────────────────────────────────────────────────
 * USAGE EXAMPLE (inside HomeScreen)
 * ─────────────────────────────────────────────────────────────
 *
 * 1) Add a state next to the other UI states:
 *
 *      var showAccessRestricted by remember { mutableStateOf(false) }
 *
 * 2) Blur the screen content while the dialog is visible.
 *    Reuse the existing blurScrim modifier on the content Box:
 *
 *      Box(
 *          modifier = Modifier
 *              .fillMaxSize()
 *              .background(Color.Transparent)
 *              .blurScrim(if (showAccessRestricted) 16.dp else sidebarBlur)
 *      ) { ... }
 *
 *    (If blurScrim is not available, Modifier.blur(16.dp) works on Android 12+.)
 *
 * 3) Show the dialog at the end of the root Box:
 *
 *      if (showAccessRestricted) {
 *          AccessRestrictedDialog(
 *              onRequestAccess = {
 *                  showAccessRestricted = false
 *
 *              },
 *              onGoBack = {
 *                  showAccessRestricted = false
 *                  goBack()
 *              },
 *              onDismiss = { showAccessRestricted = false }
 *          )
 *      }
 *
 * 4) Trigger it when the permission check fails, for example in safeNavigate():
 *
 *      if (!hasAccess) {
 *          showAccessRestricted = true
 *          return
 *      }
 */