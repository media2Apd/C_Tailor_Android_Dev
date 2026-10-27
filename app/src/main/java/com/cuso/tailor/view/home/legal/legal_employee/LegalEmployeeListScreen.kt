@file:Suppress("unused", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.legal.legal_employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.LegalEmployeeItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun LegalEmployeeListScreen(
    viewModel: LegalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val employeeList by viewModel.legalEmployees.collectAsState()
    val isLoading by viewModel.isLoadingEmployees.collectAsState()
    val errorMessage by viewModel.employeesError.collectAsState()

    // Trigger API call when entering the screen
    LaunchedEffect(Unit) {
        viewModel.fetchLegalEmployees()
    }

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<LegalEmployeeItem?>(null) }

    FabScaffold(
        fab = FabConfig(
            label = "Add Employee",
            icon = Icons.Default.Add,
            onClick = onNavigateToCreate
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TitleBar(
                title = "Legal",
                onClose = onNavigateBack
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search SO number, customer...",
                filterCount = 0,
                onFilterClick = {}
            )

            when {
                isLoading -> {
                    ListSkeleton()
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = errorMessage ?: "Error loading records",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                }
                else -> {
                    val filtered = remember(employeeList, searchQuery) {
                        employeeList.filter { item ->
                            val name = item.employeeId?.displayName ?: item.employeeCode
                            val id = item.legalRecordNumber
                            name.contains(searchQuery, ignoreCase = true) || id.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            LegalEmployeeCard(
                                item = item,
                                onEdit = { onNavigateToEdit(item.id) },
                                onView = { onNavigateToDetail(item.id) },
                                onDelete = { itemToDelete = item }
                            )
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { target ->
        DeleteModel(
            title = "Delete Legal Record",
            message = "Are you sure you want to delete legal record ${target.legalRecordNumber}? This action cannot be undone.",
            checkboxText = "Yes, delete this record",
            cancelText = "Cancel",
            confirmText = "Delete",
            onDismiss = { itemToDelete = null },
            onDelete = {
                viewModel.deleteLegalEmployee(
                    recordId = target.id,
                    onSuccess = { itemToDelete = null },
                    onError = { itemToDelete = null }
                )
            }
        )
    }
}

@Composable
private fun LegalEmployeeCard(
    item: LegalEmployeeItem,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isPending = item.status.equals("Pending", ignoreCase = true)
    val badgeBg = if (isPending) yellowBg else greenBg
    val badgeText = if (isPending) yellowText else greentext

    val (expiryDateFormatted, daysRemainingText) = remember(item.endDate) {
        formatExpiry(item.endDate)
    }

    val actions = remember {
        listOf(
            MenuAction(label = "Edit", onClick = onEdit),
            MenuAction(label = "View Details", onClick = onView),
            MenuAction(label = "Delete", onClick = onDelete)
        )
    }

    DataCard(
        item = item,
        title = item.employeeId?.displayName?.takeIf { it.isNotBlank() } ?: item.employeeCode,
        topBadgeText = item.status,
        topBadgeTextColor = badgeText,
        topBadgeBgColor = badgeBg,
        actions = actions,
        onClick = { onView() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Record ID",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = item.legalRecordNumber,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Role",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = item.employeeId?.displayName?.takeIf { it.isNotBlank() } ?: "Tailor",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Department",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = item.departmentId?.name?.takeIf { it.isNotBlank() } ?: "Production",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stat Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(Primary_background)
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                        .padding(tokens.extraPadding),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Branch", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText)
                        Text(
                            item.branchId?.name?.takeIf { it.isNotBlank() } ?: "India/Chennai",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Type", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText)
                        Text(
                            item.employmentType.ifEmpty { "Full Time" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Risk", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = mutedText)
                        Text(
                            item.riskLevel.ifEmpty { "Low" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Expiry Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Expiry: ",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = "$expiryDateFormatted ",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (daysRemainingText.isNotEmpty()) {
                        Text(
                            text = "($daysRemainingText)",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                }
            }
        }
    )
}

private fun formatExpiry(rawDate: String): Pair<String, String> {
    if (rawDate.isBlank()) return Pair("N/A", "")
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(rawDate) ?: return Pair(rawDate.take(10), "")
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val formattedDate = formatter.format(date)

        val diff = date.time - System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val remainingText = if (days >= 0) "$days remaining" else "Expired"
        Pair(formattedDate, remainingText)
    } catch (_: Exception) {
        Pair(rawDate.take(10), "")
    }
}