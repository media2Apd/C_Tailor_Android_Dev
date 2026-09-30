package com.cuso.tailor.view.home.hr.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.ShiftItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun ShiftListOverviewScreen(
    viewModel: HrViewModel,
    onClose: () -> Unit = {},
    onAddShiftClick: () -> Unit = {},
    onEditShift: (ShiftItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    LaunchedEffect(Unit) {
        viewModel.fetchShiftsViewAll()
    }

    val shifts by viewModel.shifts.collectAsState()
    val isLoading by viewModel.isLoadingShifts.collectAsState()
    val errorMsg by viewModel.shiftsError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredShifts = remember(shifts, searchQuery) {
        if (searchQuery.isBlank()) {
            shifts
        } else {
            shifts.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.department.contains(searchQuery, ignoreCase = true) ||
                        it.shiftType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add Shift",
            icon = Icons.Default.Add,
            onClick = onAddShiftClick,
            alignment = Alignment.BottomEnd,
            bottomPadding = tokens.screenPadding,
            endPadding = tokens.screenPadding
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Screen Header using standard TitleBar
            TitleBar(
                title = "Shift List Overview",
                onClose = onClose
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.4f))

            // Search Bar
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Shift...",
                    isSearchBarAlone = true
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            // UI States: Loading, Error, Empty, List
            when {
                isLoading && shifts.isEmpty() -> {
                    ListSkeleton()
                }

                errorMsg != null && shifts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = tokens.screenPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = errorMsg ?: "Failed to load shifts",
                                color = redText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { viewModel.fetchShiftsViewAll() },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Retry",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                filteredShifts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No shifts found.",
                            color = headerGrey,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                else -> {
                    // Real shifts fetched from API mapped onto DataCard
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            items = filteredShifts,
                            key = { it.id }
                        ) { shift ->
                            val badgeBg = if (shift.isActive) greenBg else redBg
                            val badgeText = if (shift.isActive) greentext else redText

                            DataCard(
                                item = shift,
                                title = shift.title,
                                titleColor = title_color,
                                titleFontWeight = FontWeight.Medium,
                                subtitle = shift.department,
                                topBadgeText = if (shift.isActive) "Active" else "Inactive",
                                topBadgeBgColor = badgeBg,
                                topBadgeTextColor = badgeText,
                                topBadgeInline = true,
                                topBadgeShowDot = false,
                                showHeaderDivider = true,
                                actions = listOf(
                                    MenuAction(
                                        label = "Edit Shift",
                                        icon = Icons.Default.Edit,
                                        onClick = { onEditShift(shift) }
                                    )
                                ),
                                content = {
                                    // Two-column Shift statistics
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Left details (START TIME & BREAK DURATION)
                                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "START TIME",
                                                    fontSize = tokens.label,
                                                    color = headerGrey,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = shift.startTime,
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = "BREAK DURATION",
                                                    fontSize = tokens.label,
                                                    color = headerGrey,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = shift.breakDuration,
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }

                                        // Right details (END TIME & SHIFT TYPE)
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = "END TIME",
                                                    fontSize = tokens.label,
                                                    color = headerGrey,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = shift.endTime,
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = "SHIFT TYPE",
                                                    fontSize = tokens.label,
                                                    color = headerGrey,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = shift.shiftType,
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}