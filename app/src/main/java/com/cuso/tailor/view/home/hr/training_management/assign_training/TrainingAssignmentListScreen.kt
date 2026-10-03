@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection")

package com.cuso.tailor.view.home.hr.training_management.assign_training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.cuso.tailor.model.hr.AssignedTrainingProgramDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
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
import com.cuso.tailor.view.home.hr.training_management.training_program.ProgramAttributeRow
import com.cuso.tailor.viewmodel.HrViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TrainingAssignmentListScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onAssignTrainingClick: () -> Unit,
    onEditAssignmentClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val assignments by viewModel.assignedTrainingPrograms.collectAsStateWithLifecycle()
    val listState by viewModel.assignedTrainingListState.collectAsStateWithLifecycle()
    val isLoading = listState is UiState.Loading

    val selectedMap = remember { mutableStateMapOf<String, Boolean>() }
    var itemToDelete by remember { mutableStateOf<AssignedTrainingProgramDto?>(null) }

    // Fetch assigned training programs from API
    LaunchedEffect(Unit) {
        viewModel.fetchAssignedTrainingPrograms()
    }

    val filteredAssignments = remember(assignments, searchQuery) {
        if (searchQuery.isBlank()) assignments
        else assignments.filter {
            it.getProgramTitle().contains(searchQuery, ignoreCase = true) ||
                    it.getPrimaryMemberName().contains(searchQuery, ignoreCase = true) ||
                    it.mode.contains(searchQuery, ignoreCase = true)
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Assign Training",
            icon = Icons.Default.Add,
            onClick = onAssignTrainingClick
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                TitleBar(
                    title = "Training Assignment",
                    onClose = onClose
                )

                Spacer(modifier = Modifier.height(4.dp))

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.fetchAssignedTrainingPrograms(search = it.ifBlank { null })
                    },
                    placeholder = "Search Programs, employee...",
                    showFilterIcon = true,
                    onFilterClick = { }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (isLoading && assignments.isEmpty()) {
                        ListSkeleton()
                    } else if (filteredAssignments.isEmpty()) {
                        Text(
                            text = "No training assignments found",
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredAssignments, key = { it.id }) { item ->
                                TrainingAssignmentCard(
                                    assignment = item,
                                    isSelected = selectedMap[item.id] == true,
                                    onToggleSelection = { selectedMap[item.id] = it },
                                    onEdit = { onEditAssignmentClick(item.id) },
                                    onDeleteClick = { itemToDelete = item }
                                )
                            }
                        }
                    }
                }
            }

            // ── Reusable Delete Confirmation Dialog ──
            if (itemToDelete != null) {
                DeleteModel(
                    title = "Delete Training Assignment",
                    message = "Are you sure you want to delete this assignment for \"${itemToDelete?.getProgramTitle()}\"?\nThis action cannot be undone.",
                    onDismiss = { itemToDelete = null },
                    onDelete = {
                        val targetId = itemToDelete?.id
                        itemToDelete = null
                        if (!targetId.isNullOrBlank()) {
                            viewModel.deleteAssignedTraining(
                                id = targetId,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Assigned training deleted successfully")
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
 * Assignment card item using DataCard and 3-dot ActionDropdownMenu.
 */
@Composable
private fun TrainingAssignmentCard(
    assignment: AssignedTrainingProgramDto,
    isSelected: Boolean,
    onToggleSelection: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val menuActions = remember {
        listOf(
            MenuAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                textColor = TextPrimary,
                tint = mutedText,
                onClick = onEdit
            ),
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = onDeleteClick
            )
        )
    }

    DataCard(
        item = assignment,
        headerContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = assignment.getProgramTitle(),
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = close_color
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadge(
                        text = assignment.status.ifBlank { "Assigned" },
                        bgColor = primary_light,
                        textColor = Primary,
                        showDot = false
                    )

                    ActionDropdownMenu(
                        icon = Icons.Default.MoreVert,
                        actions = menuActions
                    )
                }
            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProgramAttributeRow(label = "Type", value = assignment.getTrainingType())
                ProgramAttributeRow(
                    label = "Schedule",
                    value = formatSchedule(assignment.startDate, assignment.endDate)
                )
                ProgramAttributeRow(label = "Mode", value = assignment.mode.replace("_", "-"))
                ProgramAttributeRow(label = "Trainee", value = assignment.getPrimaryMemberName())

            }
        }
    )
}

private fun formatSchedule(start: String?, end: String?): String {
    if (start.isNullOrBlank()) return "Flexible"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startDate = parser.parse(start)
        val endDate = end?.let { parser.parse(it) }

        if (startDate != null && endDate != null) {
            "${formatter.format(startDate)} - ${formatter.format(endDate)}"
        } else if (startDate != null) {
            formatter.format(startDate)
        } else "Flexible"
    } catch (_: Exception) {
        start.take(10)
    }
}