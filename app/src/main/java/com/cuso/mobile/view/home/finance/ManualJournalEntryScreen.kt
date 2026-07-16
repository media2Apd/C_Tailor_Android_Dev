@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ")
package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.finance.JournalEntryItem
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.home.reusablecomposables.DataCard
import com.cuso.mobile.view.home.reusablecomposables.FabConfig
import com.cuso.mobile.view.home.reusablecomposables.FabScaffold
import com.cuso.mobile.view.home.reusablecomposables.MenuAction
import com.cuso.mobile.viewmodel.FinanceViewModel

private val BluePrimary = Color(0xFF3A2FCB)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF9A9AA8)
private val BorderGray = Color(0xFFE8E8ED)
private val GreenBg = Color(0xFFE3F7EA)
private val GreenText = Color(0xFF1FA751)
private val OrangeBg = Color(0xFFFDEFE0)
private val OrangeText = Color(0xFFE08A2C)

private fun journalStatusColors(status: String): Pair<Color, Color> {
    val isPosted = status.equals("Posted", ignoreCase = true)
    return if (isPosted) GreenBg to GreenText else OrangeBg to OrangeText
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualJournalEntryScreen(
    onClose: () -> Unit = {},
    onViewJournal: (JournalEntryItem) -> Unit = {},
    onEditJournal: (JournalEntryItem) -> Unit = {},
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    // ✅ NEW — local flag, same pattern as ChartOfAccountScreen's showAddAccount
    var showCreateJournal by remember { mutableStateOf(false) }

    if (showCreateJournal) {
        JournalEntryFormScreen(
            onClose = { showCreateJournal = false },
            onSaved = {
                showCreateJournal = false
                financeViewModel.fetchJournalEntries()   // refresh list after posting
            }
        )
        return
    }

    val entries by financeViewModel.journalEntries.collectAsStateWithLifecycle()
    val isLoading by financeViewModel.isLoadingJournalEntries.collectAsStateWithLifecycle()
    val errorMessage by financeViewModel.journalEntriesError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        financeViewModel.fetchJournalEntries()
    }

    val filteredEntries = remember(entries, searchQuery) {
        val manualOnly = entries.filter { it.isManual }

        if (searchQuery.isBlank()) manualOnly
        else manualOnly.filter {
            it.primaryAccountName.contains(searchQuery, ignoreCase = true) ||
                    it.entryNumber.contains(searchQuery, ignoreCase = true) ||
                    it.primaryAccountCode.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Title bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manual Journal Entry", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color(0xFF111827),
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onClose() }
            )
        }

        Column(Modifier.background(Color(0xFFF7F7FA))) {
            // ── Breadcrumb ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F7FA))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("Finance", color = TextSecondary, fontSize = 13.sp)
                Text("  >  ", color = TextSecondary, fontSize = 13.sp)
                Text("Journal Entry", color = BluePrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // ── Search + Filter ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Customers...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderGray,
                        focusedBorderColor = BluePrimary
                    )
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TextPrimary)
                }
            }
        }

        // ── Content ──
        when {
            isLoading -> {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    CirculerProgressIndicatorReuse()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage ?: "Something went wrong", color = Color.Red)
                }
            }
            filteredEntries.isEmpty() -> {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Text("No journal entries found", color = TextSecondary)
                }
            }
            else -> {
                FabScaffold(
                    modifier = Modifier.weight(1f),
                    fab = FabConfig(
                        label = "New Journal",
                        icon = Icons.Default.Add,
                        onClick = { showCreateJournal = true }
                    )
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredEntries, key = { it.id }) { entry ->
                            val (badgeBg, badgeFg) = journalStatusColors(entry.status)

                            // ✅ Reusing the shared DataCard component — same pattern
                            // as ChartOfAccountScreen. title/subtitle/status badge/
                            // actions all come straight from the journal entry's
                            // first line (representative account for the entry).
                            DataCard(
                                item = entry,
                                // ✅ Entry No is now the title (matches website table's first column)
                                title = entry.entryNumber,
                                titleColor = Color(0xFF111827),
                                titleFontSize = 18.sp,
                                // ✅ CHANGED — subtitle now shows ONLY Account Name + Code + Type
                                // (Sub/category removed — not one of the required fields)
                                subtitle = "${entry.primaryAccountName}   •   Code: ${entry.primaryAccountCode}   •   Type: ${entry.primaryAccountType}",
                                topBadgeText = entry.status,
                                topBadgeTextColor = badgeFg,
                                topBadgeBgColor = badgeBg,
                                topBadgeInline = true,
                                actions = listOf(
                                    MenuAction(
                                        label = "View",
                                        icon = Icons.Default.Visibility,
                                        onClick = { onViewJournal(entry) }
                                    ),
                                    MenuAction(
                                        label = "Edit",
                                        icon = Icons.Default.Edit,
                                        onClick = { onEditJournal(entry) }
                                    ),
                                    MenuAction(
                                        label = "Delete",
                                        icon = Icons.Default.Delete,
                                        tint = Color(0xFFDC2626),
                                        textColor = Color(0xFFDC2626),
                                        onClick = { /* TODO: delete journal entry API when available */ }
                                    )
                                )
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}