package com.cuso.mobile.view.home.sales.settings.measurement_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.settings.MeasurementFieldItem
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.SettingsViewModel
import com.cuso.mobile.R

@Composable
fun MeasurementListScreen(
    onClose: () -> Unit,
    onAddMeasurement: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val measurementFields by viewModel.measurementFields.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingMeasurementFields.collectAsStateWithLifecycle()
    val apiError by viewModel.errorMessage.collectAsStateWithLifecycle()

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

            HorizontalDivider(color = grey_border, thickness = 1.dp)

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading && measurementFields.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    measurementFields.isEmpty() && !isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(measurementFields, key = { it.id }) { field ->
                                MeasurementDataCard(field = field)
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandError(
            message = apiError,
            onDismiss = { viewModel.clearErrorMessage() }
        )
    }
}

@Composable
fun MeasurementDataCard(field: MeasurementFieldItem) {
    val isRequired = true
    val isStatusActive = field.status?.equals("Active", ignoreCase = true) == true || field.isActive
    val statusLabel = field.status?.uppercase() ?: if (field.isActive) "ACTIVE" else "DRAFT"

    DataCard(
        item = field,
        image = DataCardImage(
            painter = painterResource(R.drawable.ic_ruler),
            backgroundColor = primary_light,
            tint = Primary,
            size = 25.dp,
            shape = RoundedCornerShape(8.dp)
        ),
        title = field.displayName ?: field.name,
        subtitle = "${field.inputType} · ${field.unit ?: "N/A"}",
        topBadgeText = if (isRequired) "REQUIRED" else "OPTIONAL",
        topBadgeBgColor = if (isRequired) Color(0xFFFFEBEB) else Color(0xFFF1F5F9),
        topBadgeTextColor = if (isRequired) redText else Color(0xFF64748B),
        topBadgeShowDot = false,
        topBadgeInline = true,
        bottomBadgeText = statusLabel,
        bottomBadgeBgColor = if (isStatusActive) Color(0xFFE6F7ED) else Color(0xFFFEF3C7),
        bottomBadgeTextColor = if (isStatusActive) Color(0xFF10B981) else Color(0xFFD97706),
        bottomBadgeDotColor = if (isStatusActive) Color(0xFF10B981) else Color(0xFFD97706),
        showDivider = true,
        actions = listOf(
            MenuAction(label = "Edit", onClick = {}),
            MenuAction(label = "Delete", textColor = redText, onClick = {})
        )
    )
}