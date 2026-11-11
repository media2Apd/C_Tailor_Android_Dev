package com.cuso.tailor.view.home.security.support.support_ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.SupportTicketItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatLeadDate
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun SupportTicketsListScreen(
    onClose: () -> Unit,
    onCreateTicket: () -> Unit,
    onViewTicket: (String) -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    // Trigger initial fetch on screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchSupportTickets(isLoadMore = false)
    }

    // Observe pagination and search states from ViewModel
    val pagingState by viewModel.supportTicketsPagingState.collectAsState()
    val searchQuery by viewModel.ticketSearchQuery.collectAsState()

    // Filter drawer state and filter categories
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("open", "Open"),
                        FilterOption("in_progress", "In Progress"),
                        FilterOption("waiting", "Waiting"),
                        FilterOption("resolved", "Resolved"),
                        FilterOption("closed", "Closed")
                    ),
                    isMultiSelect = false
                ),
                FilterSection(
                    title = "Priority",
                    options = listOf(
                        FilterOption("low", "Low"),
                        FilterOption("medium", "Medium"),
                        FilterOption("high", "High"),
                        FilterOption("critical", "Critical")
                    ),
                    isMultiSelect = false
                )
            )
        )
    }

    val selectedFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        fab = FabConfig(
            label = "Create Ticket",
            icon = Icons.Default.Add,
            onClick = onCreateTicket
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Support Tickets",
                onClose = onClose
            )

            // Search and Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { query ->
                    viewModel.onTicketSearchChange(query)
                },
                placeholder = "Search by ticket, subject, or issue...",
                filterCount = selectedFilterCount,
                onFilterClick = {
                    filterDrawerState.open()
                }
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                PaginatedDataList(
                    state = pagingState,
                    onLoadMore = {
                        viewModel.loadMoreSupportTickets()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { ticket: SupportTicketItem ->
                    val (badgeBg, badgeText) = when (ticket.status.lowercase()) {
                        "resolved", "closed" -> greenBg to greentext
                        "in_progress", "waiting" -> yellowBg to yellowText
                        "open" -> primary_light to Primary
                        else -> redBg to redText
                    }

                    val ticketId = ticket.id.ifBlank { ticket.ticketNumber }

                    DataCard(
                        item = ticket,
                        title = ticket.subject,
                        code = ticket.ticketNumber,
                        dateText = formatLeadDate(ticket.createdAt),
                        topBadgeText = ticket.status.uppercase(),
                        topBadgeBgColor = badgeBg,
                        topBadgeTextColor = badgeText,
                        footerFields = listOf(
                            DataCardField(
                                label = "Priority",
                                text = ticket.priority.uppercase(),
                                asColumn = true
                            ),
                            DataCardField(
                                label = "Issue Type",
                                text = ticket.issueType,
                                asColumn = true
                            )
                        ),
                        actions = listOf(
                            MenuAction(
                                label = "View Details",
                                icon = Icons.Default.Visibility,
                                onClick = { onViewTicket(ticketId) }
                            )
                        ),
                        onClick = { onViewTicket(ticketId) }
                    )
                }
            }
        }

        // Animated Slide-in Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Tickets",
            sections = filterSections,
            onApply = { updatedSections ->
                filterSections = updatedSections
                val selectedStatus = updatedSections
                    .find { it.title == "Status" }
                    ?.options?.firstOrNull { it.isSelected }?.label
                val selectedPriority = updatedSections
                    .find { it.title == "Priority" }
                    ?.options?.firstOrNull { it.isSelected }?.label

                viewModel.applyTicketFilters(selectedStatus, selectedPriority)
            },
            onClearAll = {
                filterSections = filterSections.map { section ->
                    section.copy(options = section.options.map { it.copy(isSelected = false) })
                }
                viewModel.applyTicketFilters(null, null)
            }
        )
    }
}