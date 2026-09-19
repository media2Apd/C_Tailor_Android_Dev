package com.cuso.tailor.view.home.sales.settings.measurement_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.MeasurementFieldItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel
import com.cuso.tailor.R
import com.cuso.tailor.view.home.sales.settings.garment.ToggleSegmentStatusDialog

@Composable
fun MeasurementListScreen(
    onClose: () -> Unit,
    onAddMeasurement: () -> Unit,
    onEditMeasurement: (MeasurementFieldItem) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val measurementFields by viewModel.measurementFields.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingMeasurementFields.collectAsStateWithLifecycle()
    val apiError by viewModel.dynamicErrorMessage.collectAsStateWithLifecycle()
    val successMsg by viewModel.dynamicSuccessMessage.collectAsStateWithLifecycle()

    var statusDialogTarget by remember { mutableStateOf<Pair<MeasurementFieldItem, String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchMeasurementFields()
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add Measurement",
            icon = Icons.Default.Add,
            onClick = onAddMeasurement
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Measurement List",
                onClose = onClose
            )

            HorizontalDivider(color = dividerColor, thickness = 2.dp)

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && measurementFields.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(tokens.iconSize * 1.5f)
                            )
                        }
                    }
                    measurementFields.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No measurement fields found",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = tokens.extraPadding * 0.5f,
                                bottom = tokens.buttonHeight * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                        ) {
                            items(measurementFields, key = { it.id }) { field ->
                                MeasurementDataCard(
                                    field = field,
                                    onEditClick = { onEditMeasurement(field) },
                                    onToggleStatus = { targetField, nextStatus ->
                                        statusDialogTarget = Pair(targetField, nextStatus)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        statusDialogTarget?.let { (field, nextStatus) ->
            val isActivating = nextStatus.equals("Active", ignoreCase = true)
            ToggleSegmentStatusDialog(
                isActivating = isActivating,
                segmentName = field.displayName ?: field.name,
                entityLabel = "Measurement Field",
                onDismiss = {
                    statusDialogTarget = null
                },
                onConfirm = {
                    statusDialogTarget = null
                    viewModel.changeMeasurementFieldStatus(
                        fieldId = field.id,
                        nextStatus = nextStatus
                    )
                }
            )
        }

        DynamicIslandSuccess(
            message = successMsg,
            onDismiss = { viewModel.clearSuccessMessage() }
        )

        DynamicIslandError(
            message = apiError,
            onDismiss = { viewModel.clearDynamicErrorMessage() }
        )
    }
}

@Composable
fun MeasurementDataCard(
    field: MeasurementFieldItem,
    onEditClick: () -> Unit = {},
    onToggleStatus: (MeasurementFieldItem, String) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current

    val currentStatus = when {
        field.status?.equals("Active", ignoreCase = true) == true -> "Active"
        field.status?.equals("Draft", ignoreCase = true) == true -> "Draft"
        field.status?.equals("Inactive", ignoreCase = true) == true -> "Inactive"
        else -> "Inactive"
    }

    val isActive = currentStatus.equals("Active", ignoreCase = true)
    val nextStatusPayload = if (isActive) "Inactive" else "Active"

    val badgeBgColor = when (currentStatus) {
        "Active" -> greenBg
        "Draft" -> yellowBg
        else -> modelGray
    }

    val badgeTextColor = when (currentStatus) {
        "Active" -> greentext
        "Draft" -> yellowText
        else -> close_color
    }

    DataCard(
        item = field,
        image = DataCardImage(
            painter = painterResource(R.drawable.ic_ruler),
            backgroundColor = primary_light,
            tint = Primary,
            size = tokens.iconSize * 1.6f,
            shape = CircleShape
        ),
        title = field.displayName ?: field.name,
        subtitle = "${field.inputType} · ${field.unit ?: "N/A"}",
        topBadgeText = currentStatus.uppercase(),
        topBadgeBgColor = badgeBgColor,
        topBadgeTextColor = badgeTextColor,
        topBadgeShowDot = true,
        topBadgeDotColor = badgeTextColor,
        topBadgeInline = true,
        showDivider = true,
        actions = listOf(
            MenuAction(
                label = "Edit",
                onClick = onEditClick
            ),
            MenuAction(
                label = nextStatusPayload,
                textColor = if (isActive) redText else Primary,
                onClick = { onToggleStatus(field, nextStatusPayload) }
            )
        )
    )
}