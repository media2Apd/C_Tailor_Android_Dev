package com.cuso.tailor.view.home.services.settings.production_stages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.settings.ServicesProductionStageDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SettingsViewModel

@Composable
fun ProductionStagesListScreen(
    viewModel: SettingsViewModel,
    onClose: () -> Unit,
    onAddNewStage: () -> Unit,
    onEditStage: (ServicesProductionStageDto) -> Unit
) {
    val tokens = LocalAppTokens.current
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create Stage",
            icon = Icons.Default.Add,
            onClick = onAddNewStage
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            // Standard Page Header
            TitleBar(
                title = "Production Stages",
                onClose = onClose
            )

            HorizontalDivider(color = sectionBorder, thickness = 1.dp)

            Spacer(Modifier.height(8.dp))

            // Standard Reusable Search Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    viewModel.onSearchQueryChanged(query)
                },
                placeholder = "Search stage name, code or work type...",
                isSearchBarAlone = false
            )

            Spacer(Modifier.height(6.dp))

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                    isLoading && stages.isEmpty() -> {
                       ListSkeleton()
                    }

                    stages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No production stages found.",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(stages, key = { it.id }) { stage ->
                                val isActive = stage.status.equals("Active", ignoreCase = true)

                                val menuActions = listOf(
                                    MenuAction(
                                        label = "Edit Stage",
                                        onClick = { onEditStage(stage) }
                                    ),
                                    MenuAction(
                                        label = if (isActive) "Mark Inactive" else "Mark Active",
                                        textColor = if (isActive) redText else greentext,
                                        tint = if (isActive) redText else greentext,
                                        onClick = { viewModel.toggleStageStatus(stage) }
                                    )
                                )

                                val footerFields = listOf(
                                    DataCardField(
                                        label = "Work Type",
                                        text = stage.workType.ifBlank { "-" },
                                        asColumn = true
                                    ),
                                    DataCardField(
                                        label = "Worker Allocation",
                                        text = if (stage.isAllocationRequired) "Required" else "Optional",
                                        textColor = if (stage.isAllocationRequired) greentext else TextSecondary,
                                        asColumn = true
                                    ),
                                    DataCardField(
                                        label = "Rework Allowed",
                                        text = if (stage.allowRework) "Yes" else "No",
                                        textColor = if (stage.allowRework) yellowText else TextSecondary,
                                        asColumn = true
                                    )
                                )

                                DataCard(
                                    item = stage,
                                    image = DataCardImage(
                                        vector = Icons.Default.Layers,
                                        backgroundColor = primary_light,
                                        tint = Primary,
                                        size = 38.dp,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                                    ),
                                    title = stage.name,
                                    subtitle = stage.displayName.takeIf { it.isNotBlank() },
                                    code = stage.code,
                                    eyebrowText = if (stage.isSystemDefined) "System Defined (Core)" else null,
                                    eyebrowColor = headerGrey,
                                    topBadgeText = stage.status,
                                    topBadgeBgColor = if (isActive) greenBg else redBg,
                                    topBadgeTextColor = if (isActive) greentext else redText,
                                    topBadgeInline = true,
                                    footerFields = footerFields,
                                    footerAsColumns = true,
                                    actions = menuActions,
                                    onClick = { onEditStage(stage) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}