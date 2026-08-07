@file:Suppress("UNUSED_VALUE", "ASSIGNED_VALUE_IS_NEVER_READ","VariableNeverRead")
package com.cuso.mobile.view.home.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.finance.JournalEntryItem
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.viewmodel.DeleteJournalState
import com.cuso.mobile.viewmodel.FinanceViewModel

private val BluePrimary = Color(0xFF3A2FCB)
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
    financeViewModel: FinanceViewModel = hiltViewModel()
) {
    // one local state machine drives Create / View / Edit
    var formMode by remember { mutableStateOf<String?>(null) }   // null = list, "create" | "view" | "edit"
    var selectedEntry by remember { mutableStateOf<JournalEntryItem?>(null) }
    var selectedEntryId by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<JournalEntryItem?>(null) }

    val deleteJournalState by financeViewModel.deleteJournalState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deleteJournalState) {
        when (val s = deleteJournalState) {
            is DeleteJournalState.Success -> {
                deleteTarget = null                          // dialog closes immediately
                financeViewModel.resetDeleteJournalState()
                snackbarHostState.showSnackbar(s.message)     // snackbar shows after
            }
            is DeleteJournalState.Error -> {
                deleteTarget = null
                financeViewModel.resetDeleteJournalState()
                snackbarHostState.showSnackbar(s.message)
            }
            else -> {}
        }
    }

    // Create / View / Edit all reuse the same form screen
    if (formMode != null) {
        JournalEntryFormScreen(
            mode = formMode!!,
            entryId = selectedEntryId,
            onClose = {
                formMode = null
                selectedEntryId = null
                selectedEntry = null
            },
            onSaved = {
                formMode = null
                selectedEntryId = null
                selectedEntry = null
                financeViewModel.fetchJournalEntries()
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

    // Scaffold wraps everything so the delete-confirmation snackbar can show
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Transparent)
        ) {
            // ── Title bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("Manual Journal Entry", onClose = onClose)

            }

            Column {
                // ── Breadcrumb ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    ScreenBreadcrumb(listOf("Finance","Journal Entry"), onClick = {})
                }

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = "Search Journals...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { /* TODO: open filter drawer */ }
                )
            }

            // ── Content ──
            // FabScaffold is now called ONCE, always, regardless of state.
            // The when{} that switches between loading/error/empty/list lives
            // INSIDE it, as the scaffold's content lambda. Previously FabScaffold
            // was only one branch of an outer when{}, so the FAB disappeared
            // whenever loading/error/empty were true. Do NOT nest another
            // when{} branch around FabScaffold — that's what caused the
            // "Condition type mismatch: inferred type is Unit but Boolean was
            // expected" error you just hit (a composable call with no
            // condition sitting inside a `when { ... }`).
            FabScaffold(
                modifier = Modifier.weight(1f),
                fab = FabConfig(
                    label = "New Journal",
                    icon = Icons.Default.Add,
                    onClick = {
                        selectedEntry = null
                        selectedEntryId = null
                        formMode = "create"
                    }
                )
            ) {
                when {
                    isLoading -> {
                        ListSkeleton()
                    }
                    errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = errorMessage ?: "Something went wrong", color = Color.Red)
                        }
                    }
                    filteredEntries.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No journal entries found", color = TextSecondary)
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredEntries, key = { it.id }) { entry ->
                                val (badgeBg, badgeFg) = journalStatusColors(entry.status)

                                DataCard(
                                    item = entry,
                                    title = entry.entryNumber,
                                    titleColor = Color(0xFF111827),
                                    titleFontSize = 18.sp,
                                    subtitle = "${entry.primaryAccountName}   •   Code: ${entry.primaryAccountCode}   •   Type: ${entry.primaryAccountType}",
                                    topBadgeText = entry.status,
                                    topBadgeTextColor = badgeFg,
                                    topBadgeBgColor = badgeBg,
                                    topBadgeInline = true,
                                    actions = listOf(
                                        MenuAction(
                                            label = "View",
                                            icon = Icons.Default.Visibility,
                                            onClick = {
                                                selectedEntry = entry
                                                selectedEntryId = entry.id
                                                formMode = "view"
                                            }
                                        ),
                                        MenuAction(
                                            label = "Edit",
                                            icon = Icons.Default.Edit,
                                            onClick = {
                                                selectedEntry = entry
                                                selectedEntryId = entry.id
                                                formMode = "edit"
                                            }
                                        ),
                                        MenuAction(
                                            label = "Delete",
                                            icon = Icons.Default.Delete,
                                            tint = Color(0xFFDC2626),
                                            textColor = Color(0xFFDC2626),
                                            onClick = { deleteTarget = entry }
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

    // Delete confirmation dialog, sits outside the Scaffold body,
    // shows on top of everything when deleteTarget is non-null.
    // Closes immediately on success/error (see LaunchedEffect above).
    deleteTarget?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = whiteBg,
            title = { Text("Delete Journal Entry", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete \"${entry.entryNumber}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { financeViewModel.deleteJournalEntry(entry.id) }) {
                    Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}