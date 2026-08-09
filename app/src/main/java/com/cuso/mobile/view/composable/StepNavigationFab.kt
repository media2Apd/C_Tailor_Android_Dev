package com.cuso.mobile.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg

/**
 * Describes what the trailing (bottom-end) FAB should do/show.
 * Pick ONE depending on your screen's current state:
 *  - Next   -> normal "go to next step" pill
 *  - Edit   -> switches a view-only screen into edit mode / navigates to edit
 *  - Update -> submits the form (shows a spinner while is true)
 */
sealed class TrailingFabAction {
    data class Next(
        val label: String = "Next",
        val enabled: Boolean = true,
        val onClick: () -> Unit
    ) : TrailingFabAction()

    data class Edit(
        val label: String = "Edit",
        val enabled: Boolean = true,
        val onClick: () -> Unit
    ) : TrailingFabAction()

    data class Update(
        val isLoading: Boolean = false,
        val label: String = "Update",
        val enabled: Boolean = true,
        val onClick: () -> Unit
    ) : TrailingFabAction()
}

/**
 * @param showBack whether to render the Back button at all (e.g. hide on step 0)
 * @param onBack invoked when Back is tapped
 * @param backLabel text shown on the Back button (defaults to "Back")
 * @param trailingAction the right-hand action to render; pass null to hide it entirely
 * @param backWidthFraction optional fraction of screen width for the Back button
 * @param trailingWidthFraction optional fraction of screen width for the trailing button
 * @param showBackArrow whether to show arrow icon on the back button (default: true)
 * @param showTrailingArrow whether to show arrow icon on the trailing button (default: true)
 */
@Composable
fun StepNavigationFab(
    modifier: Modifier = Modifier,
    showBack: Boolean,
    onBack: () -> Unit,
    trailingAction: TrailingFabAction?,
    backLabel: String = "Back",
    backEnabled: Boolean = true,
    backWidthFraction: Float? = null,
    trailingWidthFraction: Float? = null,
    showBackArrow: Boolean = true,
    showTrailingArrow: Boolean = true
) {
    Box(modifier.fillMaxSize()) {
        if (showBack) {
            BackFabButton(
                onClick = onBack,
                label = backLabel,
                enabled = backEnabled,
                showArrow = showBackArrow,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 14.dp)
                    .let { if (backWidthFraction != null) it.fillMaxWidth(backWidthFraction) else it }
            )
        }

        if (trailingAction != null) {
            TrailingFabButton(
                action = trailingAction,
                showArrow = showTrailingArrow,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 14.dp)
                    .let { if (trailingWidthFraction != null) it.fillMaxWidth(trailingWidthFraction) else it }
            )
        }
    }
}

@Composable
fun BackFabButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    label: String = "Back",
    enabled: Boolean = true,
    showArrow: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = whiteBg,
            contentColor = Color(0xFF111827)
        ),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
    ) {
        if (showArrow) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TrailingFabButton(
    action: TrailingFabAction,
    modifier: Modifier = Modifier,
    showArrow: Boolean = true
) {
    val isEnabled = when (action) {
        is TrailingFabAction.Next -> action.enabled
        is TrailingFabAction.Edit -> action.enabled
        is TrailingFabAction.Update -> action.enabled && !action.isLoading
    }

    Button(
        onClick = {
            when (action) {
                is TrailingFabAction.Next -> action.onClick()
                is TrailingFabAction.Edit -> action.onClick()
                is TrailingFabAction.Update -> action.onClick()
            }
        },
        enabled = isEnabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = whiteBg,
            disabledContainerColor = Primary.copy(alpha = 0.4f),
            disabledContentColor = whiteBg.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
    ) {
        when (action) {
            is TrailingFabAction.Next -> {
                if (showArrow) {
                    Text(action.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                } else {
                    Text(action.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            is TrailingFabAction.Edit -> {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(action.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            is TrailingFabAction.Update -> {
                if (action.isLoading) {
                    CirculerProgressIndicatorForButton()
                } else {
                    Text(action.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}