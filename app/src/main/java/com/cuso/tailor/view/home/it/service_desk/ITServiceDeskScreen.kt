package com.cuso.tailor.view.home.it.service_desk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.TicketMetricsDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun ItServiceDeskListScreen(
    viewModel: ItViewModel,
    onNavigateBack: () -> Unit,
    onNavigateDetail: (String) -> Unit,
    onNavigateCreate: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val filterState = rememberFilterDrawerState()

    val tickets by viewModel.filteredTickets.collectAsState()
    val metrics by viewModel.ticketMetrics.collectAsState()
    val searchQuery by viewModel.ticketSearchQuery.collectAsState()
    val selectedStatus by viewModel.selectedTicketStatus.collectAsState()
    val selectedPriority by viewModel.selectedTicketPriority.collectAsState()
    val isLoading by viewModel.isLoadingTickets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTicketDashboard()
    }

    val filterSections = remember(selectedStatus, selectedPriority) {
        listOf(
            FilterSection(
                title = "Ticket Status",
                options = listOf(
                    FilterOption("Open", "Open", selectedStatus == "Open"),
                    FilterOption("In Progress", "In Progress", selectedStatus == "In Progress"),
                    FilterOption("Pending", "Pending", selectedStatus == "Pending"),
                    FilterOption("Resolved", "Resolved", selectedStatus == "Resolved"),
                    FilterOption("Closed", "Closed", selectedStatus == "Closed")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Priority",
                options = listOf(
                    FilterOption("Critical", "Critical", selectedPriority == "Critical"),
                    FilterOption("High", "High", selectedPriority == "High"),
                    FilterOption("Medium", "Medium", selectedPriority == "Medium"),
                    FilterOption("Low", "Low", selectedPriority == "Low")
                ),
                isMultiSelect = false
            )
        )
    }

    val activeFilterCount = (if (!selectedStatus.isNullOrBlank()) 1 else 0) +
            (if (!selectedPriority.isNullOrBlank()) 1 else 0)

    val fabConfig = remember(onNavigateCreate) {
        FabConfig(
            label = "Create Ticket",
            icon = Icons.Default.Add,
            onClick = onNavigateCreate
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = fabConfig
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "IT Service Desk",
                subtitle = "Support & Incidents",
                onClose = onNavigateBack,
                trailingContent = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                            .background(whiteBg)
                            .clickable { viewModel.fetchTicketDashboard() }
                            .padding(horizontal = tokens.screenPadding / 2, vertical = tokens.screenPadding / 3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = title_color,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.screenPadding / 4))
                        Text(
                            text = "Refresh",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onTicketSearchQueryChange(it) },
                placeholder = "Search tickets by ID, requester, or topic...",
                showFilterIcon = true,
                filterCount = activeFilterCount,
                onFilterClick = { filterState.open() }
            )

            metrics?.let {
                TicketMetricsGrid(metrics = it)
            }

            if (isLoading && tickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(tokens.buttonHeight)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(items = tickets, key = { it.ticketId.ifEmpty { it.id.orEmpty() } }) { ticket ->
                        val (statusBg, statusText) = getTicketStatusColors(ticket.status)

                        DataCard(
                            item = ticket,
                            title = ticket.subject,
                            titleFontWeight = FontWeight.Medium,
                            titleColor = title_color,
                            smalltitle = ticket.ticketId,
                            subtitle = ticket.category,
                            code = null,
                            topBadgeText = ticket.priority,
                            topBadgeBgColor = getPriorityBgColor(ticket.priority),
                            topBadgeTextColor = getPriorityTextColor(ticket.priority),
                            topBadgeInline = true,
                            showDivider = false,
                            actions = listOf(
                                MenuAction(
                                    label = "View Details",
                                    onClick = { onNavigateDetail(ticket.ticketId.ifEmpty { ticket.id.orEmpty() }) }
                                )
                            ),
                            onClick = { onNavigateDetail(ticket.ticketId.ifEmpty { ticket.id.orEmpty() }) },
                            footerAsColumns = true,
                            footerFields = listOf(
                                DataCardField(
                                    label = "REQUESTER",
                                    text = ticket.requester,
                                    labelColor = mutedText,
                                    textColor = TextPrimary,
                                    valueFontWeight = FontWeight.Normal
                                ),
                                DataCardField(
                                    label = "ASSIGNEE",
                                    text = ticket.assignee,
                                    labelColor = mutedText,
                                    textColor = TextPrimary,
                                    valueFontWeight = FontWeight.Normal
                                ),
                                DataCardField(
                                    label = "BRANCH",
                                    text = ticket.branch.ifBlank { "--" },
                                    labelColor = mutedText,
                                    textColor = TextPrimary,
                                    valueFontWeight = FontWeight.Normal
                                )
                            ),
                            content = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "STATUS :",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = mutedText
                                        )
                                        Spacer(Modifier.width(tokens.screenPadding / 4))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
                                                .background(statusBg)
                                                .padding(horizontal = tokens.screenPadding / 3, vertical = tokens.screenPadding / 8)
                                        ) {
                                            Text(
                                                text = ticket.status,
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = statusText
                                            )
                                        }
                                    }

                                    ticket.slaUpdateText?.let { slaText ->
                                        Text(
                                            text = slaText,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = if (ticket.slaStatus == "BREACHED") redText else orangeText
                                        )
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(tokens.buttonHeight + tokens.screenPadding))
                    }
                }
            }
        }

        FilterDrawer(
            state = filterState,
            title = "Filter Tickets",
            sections = filterSections,
            onApply = { updatedSections ->
                val statusSec = updatedSections.find { it.title == "Ticket Status" }
                val selectedStatusOption = statusSec?.options?.find { it.isSelected }?.id
                viewModel.onTicketStatusFilterChange(selectedStatusOption)

                val prioritySec = updatedSections.find { it.title == "Priority" }
                val selectedPriorityOption = prioritySec?.options?.find { it.isSelected }?.id
                viewModel.onTicketPriorityFilterChange(selectedPriorityOption)
            },
            onClearAll = {
                viewModel.clearTicketFilters()
            }
        )
    }
}

@Composable
private fun TicketMetricsGrid(metrics: TicketMetricsDto) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
        ) {
            TicketMetricCard(
                modifier = Modifier.weight(1f),
                label = "TOTAL TICKETS",
                value = metrics.totalTickets.toString(),
                valueColor = TextPrimary
            )
            TicketMetricCard(
                modifier = Modifier.weight(1f),
                label = "OPEN",
                value = metrics.open.toString(),
                valueColor = Primary,
                tagColor = Primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
        ) {
            TicketMetricCard(
                modifier = Modifier.weight(1f),
                label = "IN PROGRESS",
                value = metrics.inProgress.toString(),
                valueColor = orangeText,
                tagColor = orangeText
            )
            TicketMetricCard(
                modifier = Modifier.weight(1f),
                label = "SLA BREACHED",
                value = metrics.slaBreached.toString(),
                valueColor = redText,
                tagColor = redText
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
        ) {
            TicketMetricCard(
                modifier = Modifier.weight(1f),
                label = "RESOLVED",
                value = metrics.resolved.toString(),
                valueColor = greentext,
                tagColor = greentext
            )
            TicketMetricCard(
                modifier = Modifier.weight(1f),
                label = "CLOSED",
                value = metrics.closed.toString(),
                valueColor = headerGrey,
                tagColor = headerGrey
            )
        }
    }
}

@Composable
private fun TicketMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    tagColor: Color? = null
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding * 0.75f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = mutedText
                )
                if (tagColor != null) {
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize * 0.4f)
                            .clip(CircleShape)
                            .background(tagColor)
                    )
                }
            }
            Text(
                text = value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

fun getTicketStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "open" -> Pair(light_blue, Primary)
        "in progress" -> Pair(yellowBg, orangeText)
        "resolved" -> Pair(greenBg, greentext)
        "closed" -> Pair(light_grey, TextSecondary)
        else -> Pair(light_grey, mutedText)
    }
}

fun getPriorityBgColor(priority: String): Color {
    return when (priority.lowercase()) {
        "critical" -> redBg
        "high" -> orangeBg
        "medium" -> yellowBg
        else -> light_grey
    }
}

fun getPriorityTextColor(priority: String): Color {
    return when (priority.lowercase()) {
        "critical" -> redText
        "high" -> orangeText
        "medium" -> yellowText
        else -> TextSecondary
    }
}