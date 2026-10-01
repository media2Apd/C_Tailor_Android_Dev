@file:Suppress("unused","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.attendance_management.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.AttendanceRecord
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.AttendanceUiState
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun AttendanceListScreen(
    viewModel: HrViewModel,
    onClose: () -> Unit = {},
    onManualEntryClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    var filterDropdownOpen by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    var selectedRecordForApproval by remember { mutableStateOf<AttendanceRecord?>(null) }
    var isApproving by remember { mutableStateOf(false) }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadNextPage()
        }
    }

    FabScaffold(
        fab = FabConfig(
            label = "Manual Entry",
            icon = Icons.Default.Add,
            onClick = onManualEntryClick,
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
            TitleBar(
                title = "Attendance List",
                onClose = onClose
            )


            // Search Bar + Dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search Employee...",
                )
            }

            when (val state = uiState) {
                is AttendanceUiState.Loading -> {
                    ListSkeleton()
                }
                is AttendanceUiState.Error -> {
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
                                text = state.message,
                                color = redText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { viewModel.loadAttendance(reset = true) },
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
                is AttendanceUiState.Success -> {
                    if (state.records.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No attendance records found.",
                                color = headerGrey,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(
                                items = state.records,
                                key = { it.id }
                            ) { record ->
                                val isPresent = record.status.equals("Present", ignoreCase = true)
                                val isAbsent = record.status.equals("Absent", ignoreCase = true)

                                val badgeBg = when {
                                    isPresent -> greenBg
                                    isAbsent -> redBg
                                    else -> yellowBg
                                }
                                val badgeText = when {
                                    isPresent -> greentext
                                    isAbsent -> redText
                                    else -> yellowText
                                }

                                // Show Approve option only when approvalStatus is pending
                                val menuActions = if (record.approvalStatus.equals("pending", ignoreCase = true)) {
                                    listOf(
                                        MenuAction(
                                            label = "Approve",
                                            icon = Icons.Default.CheckCircleOutline,
                                            tint = complete_button_bg,
                                            textColor = title_color,
                                            onClick = {
                                                selectedRecordForApproval = record
                                            }
                                        )
                                    )
                                } else {
                                    emptyList()
                                }

                                DataCard(
                                    item = record,
                                    title = record.name,
                                    titleColor = title_color,
                                    titleFontWeight = FontWeight.Medium,
                                    subtitle = "${record.empCode} • ${record.department}",
                                    topBadgeText = record.status,
                                    topBadgeBgColor = badgeBg,
                                    topBadgeTextColor = badgeText,
                                    topBadgeInline = true,
                                    topBadgeShowDot = false,
                                    showHeaderDivider = true,
                                    actions = menuActions,
                                    content = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            // Left details
                                            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "SHIFT",
                                                        fontSize = tokens.label,
                                                        color = headerGrey,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = record.shift,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "IN TIME",
                                                        fontSize = tokens.label,
                                                        color = headerGrey,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = record.inTime,
                                                        fontSize = tokens.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }
                                            }

                                            // Right details
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = "TOTAL HOURS",
                                                        fontSize = tokens.label,
                                                        color = headerGrey,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = record.totalHours,
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
                                                        text = "OUT TIME",
                                                        fontSize = tokens.label,
                                                        color = headerGrey,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = record.outTime,
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

                            if (state.canLoadMore) {
                                item {
                                    ThreeDotLoading()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog matching the image
    selectedRecordForApproval?.let { record ->
        val dateText = if (record.date.isNotBlank()) " for ${record.date}" else ""
        Dialog(onDismissRequest = {
            if (!isApproving) selectedRecordForApproval = null
        }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = whiteBg,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Approve Manual Attendance",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "${record.name} (${record.empCode}) - Do you want to approve the manual attendance${dateText}? Once approved, the status will be changed to \"Present\".",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedRecordForApproval = null },
                            enabled = !isApproving,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = Color(0xFF0F172A)
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                isApproving = true
                                viewModel.approveAttendance(
                                    attendanceId = record.id,
                                    onSuccess = {
                                        isApproving = false
                                        selectedRecordForApproval = null
                                    },
                                    onError = {
                                        isApproving = false
                                        selectedRecordForApproval = null
                                    }
                                )
                            },
                            enabled = !isApproving,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEF2FF),
                                contentColor = Primary
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            if (isApproving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Primary
                                )
                            } else {
                                Text(
                                    text = "Approve",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}