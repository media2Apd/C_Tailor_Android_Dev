@file:Suppress("UNUSED_PARAMETER", "unusedVariable")

package com.cuso.mobile.view.home.sales.settings.garment.garment_category_detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.database.entities.GarmentMeasurement
import com.cuso.mobile.model.settings.GarmentStyleItem
import com.cuso.mobile.model.settings.StyleMeasurementFieldEntry
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.viewmodel.SettingsViewModel

@Composable
fun ConfigurationPreviewScreen(
    garmentStyle: GarmentStyleItem? = null,
    activeFields: List<StyleMeasurementFieldEntry> = emptyList(),
    garmentTitle: String = garmentStyle?.displayName ?: garmentStyle?.name ?: "Garment Style",
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onBackToEdit: () -> Unit = onClose,
    onActivateConfirmed: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var showActivateDialog by remember { mutableStateOf(false) }

    // Dynamic Island State
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Handle system back gesture
    BackHandler {
        onBackToEdit()
    }

    // Convert exact activeFields from Profile screen to GarmentMeasurement objects for Preview
    val displayMeasurements: List<GarmentMeasurement> = remember(activeFields.size, activeFields.toList()) {
        activeFields.mapIndexed { index, entry ->
            val detail = entry.fieldDetail
            GarmentMeasurement(
                id = entry.id ?: detail?.id ?: "",
                label = detail?.displayName ?: detail?.name ?: "Field ${index + 1}",
                unit = detail?.unit ?: "inch",
                inputType = detail?.inputType ?: "Number",
                isRequired = entry.isRequired,
                displayOrder = if (entry.displayOrder > 0) entry.displayOrder else (index + 1)
            )
        }
    }

    val requiredCount = displayMeasurements.count { it.isRequired }
    val optionalCount = displayMeasurements.size - requiredCount

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Configuration Preview",
                    onClose = onBackToEdit
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                    .padding(bottom = 90.dp)
            ) {
                Text(
                    text = garmentTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Review all measurement fields and configurations before activating.",
                    fontSize = 12.sp,
                    color = close_color,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(20.dp))

                if (displayMeasurements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No measurement fields found in configuration.",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    PreviewSectionTitle("Measurement Fields (${displayMeasurements.size})")

                    // Render all live fields active in profile config
                    displayMeasurements.forEach { item ->
                        PreviewMeasurementRow(item = item)
                    }

                    Spacer(Modifier.height(24.dp))

                    // Dynamic summary banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1EFFE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Total: ${displayMeasurements.size} Measurement Fields · $requiredCount Required · $optionalCount Optional",
                                fontSize = 12.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Floating Step Navigation FAB
        StepNavigationFab(
            showBack = true,
            onBack = onBackToEdit,
            backLabel = "Back to Edit",
            showBackArrow = true,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Activate Configuration",
                onClick = { showActivateDialog = true }
            )
        )

        // Dynamic Island Overlay
        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }

    // Activate Configuration Confirmation Dialog
    if (showActivateDialog) {
        ActivateConfigurationDialog(
            garmentName = garmentTitle,
            onDismiss = { showActivateDialog = false },
            onConfirm = {
                showActivateDialog = false
                if (garmentStyle != null) {
                    viewModel.activateGarmentStyleConfiguration(
                        style = garmentStyle,
                        measurements = displayMeasurements,
                        onSuccess = {
                            successMessage = "Configuration activated successfully"
                            onActivateConfirmed()
                        },
                        onError = { err ->
                            errorMessage = err
                        }
                    )
                } else {
                    onActivateConfirmed()
                }
            }
        )
    }
}

@Composable
private fun PreviewSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = title_color,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun PreviewMeasurementRow(item: GarmentMeasurement) {
    val unitText = if (item.unit.isNotBlank()) " · ${item.unit}" else ""
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEDE9FE), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ruler),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.inputType}$unitText",
                    fontSize = 11.sp,
                    color = iconMuted
                )
            }

            if (item.isRequired) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFEBEB), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "REQUIRED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = redText
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "OPTIONAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
        HorizontalDivider(color = dividerColor, thickness = 0.8.dp)
    }
}

@Composable
fun ActivateConfigurationDialog(
    garmentName: String = "Men's Shirt",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFEDE9FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Activate Configuration?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = title_color,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "This will make the measurement configuration for $garmentName active. All new orders will use this configuration immediately.",
                    fontSize = 12.sp,
                    color = close_color,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "This action cannot be undone without creating a new revision.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = title_color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = "Activate Now",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}