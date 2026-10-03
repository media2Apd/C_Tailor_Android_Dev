@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection")

package com.cuso.tailor.view.home.hr.training_management.training_program

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.TrainingProgramDto
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.UiState
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel

/**
 * Screen displaying the list of all training programs with 3-dot action menus and Delete confirmation modal.
 */
@Composable
fun TrainingProgramListScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onAddProgramClick: () -> Unit,
    onEditProgramClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val programs by viewModel.trainingPrograms.collectAsStateWithLifecycle()
    val listState by viewModel.trainingListState.collectAsStateWithLifecycle()
    val isLoading = listState is UiState.Loading

    // State to hold the target program to be deleted
    var programToDelete by remember { mutableStateOf<TrainingProgramDto?>(null) }

    // Fetch programs from API on launch
    LaunchedEffect(Unit) {
        viewModel.fetchTrainingPrograms()
    }

    val filteredPrograms = remember(programs, searchQuery) {
        if (searchQuery.isBlank()) programs
        else programs.filter {
            it.programTitle.contains(searchQuery, ignoreCase = true) ||
                    it.programId.contains(searchQuery, ignoreCase = true) ||
                    it.getTrainerDisplayName().contains(searchQuery, ignoreCase = true)
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add Program",
            icon = Icons.Default.Add,
            onClick = onAddProgramClick
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Header Bar
                TitleBar(
                    title = "Training Management",
                    onClose = onClose
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Search & Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.fetchTrainingPrograms(search = it.ifBlank { null })
                    },
                    placeholder = "Search Programs, employee...",
                    showFilterIcon = true,
                    onFilterClick = { }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content Section
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (isLoading && programs.isEmpty()) {
                        ListSkeleton()
                    } else if (filteredPrograms.isEmpty()) {
                        Text(
                            text = "No training programs found",
                            fontSize = tokens.bodySmall,
                            color = close_color,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredPrograms, key = { it.id }) { program ->
                                TrainingProgramCard(
                                    program = program,
                                    onEdit = { onEditProgramClick(program.id) },
                                    onDeleteClick = { programToDelete = program }
                                )
                            }
                        }
                    }
                }
            }

            // ── Reusable Delete Confirmation Dialog ──
            if (programToDelete != null) {
                DeleteModel(
                    title = "Delete Training Program",
                    message = "Are you sure you want to delete \"${programToDelete?.programTitle}\"?\nThis action cannot be undone.",
                    onDismiss = { programToDelete = null },
                    onDelete = {
                        val targetId = programToDelete?.id
                        programToDelete = null
                        if (!targetId.isNullOrBlank()) {
                            viewModel.deleteTrainingProgram(
                                id = targetId,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Training program deleted successfully")
                                },
                                onError = { errorMsg ->
                                    DynamicIslandManager.showError(errorMsg)
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * Reusable Training Program Card with a 3-dot action menu.
 */
@Composable
private fun TrainingProgramCard(
    program: TrainingProgramDto,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Action menu items for Edit and Delete
    val menuActions = remember {
        listOf(
            MenuAction(
                label = "Edit",
                textColor = TextPrimary,
                tint = close_color,
                onClick = onEdit
            ),
            MenuAction(
                label = "Delete",
                textColor = redText,
                tint = redText,
                onClick = onDeleteClick
            )
        )
    }

    DataCard(
        item = program,
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Program ID
                Text(
                    text = program.programId,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = close_color
                )

                // Top Right: Status Badge + 3-Dot Action Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        text = if (program.isActive) "Active" else "Inactive",
                        bgColor = if (program.isActive) greenBg else redBg,
                        textColor = if (program.isActive) greentext else redText,
                        showDot = false
                    )

                    ActionDropdownMenu(
                        icon = Icons.Default.MoreVert,
                        actions = menuActions
                    )
                }
            }
        },
        title = program.programTitle,
        titleColor = title_color,
        titleFontWeight = FontWeight.SemiBold,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProgramAttributeRow(label = "Type", value = program.trainingType)
                ProgramAttributeRow(
                    label = "Duration",
                    value = "${program.duration.toInt()} ${program.durationMethod ?: "Hours"}"
                )
                ProgramAttributeRow(
                    label = "Mode",
                    value = program.modeOfDelivery.replace("_", "-")
                )
                ProgramAttributeRow(label = "Trainer", value = program.getTrainerDisplayName())
            }
        }
    )
}

@Composable
fun ProgramAttributeRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.caption, color = close_color)
        Text(text = value, fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}