@file:Suppress("unused","AssignedValueIsNeverRead", "unusedVariable")

package com.cuso.tailor.view.home.hr.organization_structure

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.PRODUCTION_SPECIALIZATIONS
import com.cuso.tailor.model.hr.ProductionTeamDto
import com.cuso.tailor.model.hr.activeMembers
import com.cuso.tailor.model.hr.availabilityStatus
import com.cuso.tailor.model.hr.displayStatus
import com.cuso.tailor.model.hr.workloadValue
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.blueText
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.PageLoader
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.HrViewModel

private fun availabilityColors(availability: String): Pair<Color, Color> = when (availability) {
    "Available" -> Pair(greentext, greenBg)
    "Near Capacity" -> Pair(yellowText, yellowBg)
    "Fully Booked" -> Pair(redText, redBg)
    else -> Pair(TextSecondary, light_grey)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductionTeamsScreen(
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCreateTeam: () -> Unit = {},
    onViewTeam: (String) -> Unit = {},
    onEditTeam: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val pagingState by hrViewModel.productionTeamsPagingState.collectAsStateWithLifecycle()
    val allTeams by hrViewModel.allProductionTeams.collectAsStateWithLifecycle()
    val kpi by hrViewModel.productionTeamKpi.collectAsStateWithLifecycle()
    val errorMessage by hrViewModel.productionTeamsError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var teamToDelete by remember { mutableStateOf<ProductionTeamDto?>(null) }

    val filterDrawerState = rememberFilterDrawerState()
    val locationOptions = remember(allTeams) {
        allTeams.map { it.location.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .map { FilterOption(id = it, label = it) }
    }

    var filterSections by remember(locationOptions) {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Specialization",
                    options = PRODUCTION_SPECIALIZATIONS.map { FilterOption(id = it.code, label = it.name) },
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Studio",
                    options = locationOptions,
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Status",
                    options = listOf("Active", "Inactive", "Draft", "Archived").map {
                        FilterOption(id = it, label = it)
                    },
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Availability",
                    options = listOf("Available", "Near Capacity", "Fully Booked", "Unavailable").map {
                        FilterOption(id = it, label = it)
                    },
                    isMultiSelect = true
                )
            )
        )
    }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { sec -> sec.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        hrViewModel.fetchProductionTeams()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Production Teams",
                onClose = onClose,
                trailingContent = {
                    StatusBadge(
                        text = "${kpi.totalTeams} Teams",
                        bgColor = primary_light,
                        textColor = Primary
                    )
                }
            )

            if (pagingState.isInitialLoading) {
                // Show full-screen PageLoader while production teams data loads
                PageLoader(
                    modifier = Modifier.weight(1f),
                    label = "Loading production teams",
                    messages = listOf("Fetching teams", "Checking availability", "Almost done"),
                    fullScreen = true
                )
            } else {
                SearchFilterBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    hrViewModel.onProductionTeamSearchChanged(it)
                },
                placeholder = "Search by team name, code, or lead...",
                filterCount = activeFilterCount,
                accentColor = Primary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { filterDrawerState.open() }
            )


            // 2x2 KPI summary grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    ProductionKpiCard(
                        label = "TOTAL TEAMS",
                        value = kpi.totalTeams.toString(),
                        footnote = "Across ${kpi.facilitiesCount} facilities",
                        icon = Icons.Default.Layers,
                        iconBg = light_grey,
                        iconTint = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    ProductionKpiCard(
                        label = "ACTIVE TEAMS",
                        value = kpi.activeTeams.toString(),
                        badgeText = "${kpi.activePercentage}%",
                        footnote = "In operational rotation",
                        icon = Icons.Default.CheckCircle,
                        iconBg = greenBg,
                        iconTint = greentext,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    ProductionKpiCard(
                        label = "TOTAL MEMBERS",
                        value = kpi.totalMembers.toString(),
                        footnote = "Specialists & artisans",
                        icon = Icons.Default.Groups,
                        iconBg = primary_light,
                        iconTint = blueText,
                        modifier = Modifier.weight(1f)
                    )
                    ProductionKpiCard(
                        label = "AVAILABLE",
                        value = kpi.availableTeams.toString(),
                        badgeText = "Open",
                        footnote = "Ready for dispatches",
                        icon = Icons.Default.CalendarToday,
                        iconBg = greenBg,
                        iconTint = greentext,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val isErrorState = errorMessage != null &&
                    pagingState.items.isEmpty()

            FabScaffold(
                modifier = Modifier.fillMaxSize(),
                fab = FabConfig(
                    label = "Create Team",
                    icon = Icons.Default.Add,
                    onClick = onCreateTeam
                ),
                fabVisible = !isErrorState
            ) {
                when {
                    isErrorState -> {
                        AppErrorState(
                            title = "Failed to load production teams",
                            message = errorMessage ?: "Please check your connection and try again.",
                            onRetry = { hrViewModel.fetchProductionTeams() }
                        )
                    }

                    pagingState.items.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 4),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.imageDp)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .background(background_light_purple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 1.5f)
                                )
                            }
                            Spacer(Modifier.height(tokens.extraPadding))
                            Text(
                                text = if (searchQuery.isNotBlank() || activeFilterCount > 0) {
                                    "No Matching Production Teams"
                                } else {
                                    "No Production Teams Found"
                                },
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                            Text(
                                text = if (searchQuery.isNotBlank() || activeFilterCount > 0) {
                                    "Try adjusting your search or filter selections"
                                } else {
                                    "Create your first permanent production team"
                                },
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        val horizontalScrollState = rememberScrollState()

                        // Fixed column widths derived from adaptive tokens for aligned header and rows
                        val colTeam = tokens.fieldHeight * 4.2f
                        val colLead = tokens.fieldHeight * 4.0f
                        val colMembers = tokens.fieldHeight * 2.8f
                        val colSpecs = tokens.fieldHeight * 4.6f
                        val colLocation = tokens.fieldHeight * 3.2f
                        val colWorkload = tokens.fieldHeight * 3.4f
                        val colAvailability = tokens.fieldHeight * 3.4f
                        val colStatus = tokens.fieldHeight * 2.2f
                        val colActions = tokens.fieldHeight * 1.6f

                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(tokens.extraPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.75f),
                            color = whiteBg,
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                // Table Column Headers
                                Row(
                                    modifier = Modifier
                                        .background(PanelBg)
                                        .padding(
                                            horizontal = tokens.screenPadding,
                                            vertical = tokens.extraPadding
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TableHeaderCell("TEAM NAME / CODE", colTeam)
                                    TableHeaderCell("TEAM LEAD", colLead)
                                    TableHeaderCell("MEMBERS", colMembers)
                                    TableHeaderCell("SPECIALIZATIONS", colSpecs)
                                    TableHeaderCell("LOCATION", colLocation)
                                    TableHeaderCell("WORKLOAD", colWorkload)
                                    TableHeaderCell("AVAILABILITY", colAvailability)
                                    TableHeaderCell("STATUS", colStatus)
                                    TableHeaderCell("ACTIONS", colActions)
                                }
                                HorizontalDivider(color = sectionBorder)

                                // Vertically Paginated Table Rows
                                PaginatedDataList(
                                    state = pagingState,
                                    onLoadMore = { hrViewModel.loadMoreProductionTeams() },
                                    keySelector = { it.id },
                                    modifier = Modifier.weight(1f)
                                ) { team ->
                                    ProductionTeamTableRow(
                                        team = team,
                                        colTeam = colTeam,
                                        colLead = colLead,
                                        colMembers = colMembers,
                                        colSpecs = colSpecs,
                                        colLocation = colLocation,
                                        colWorkload = colWorkload,
                                        colAvailability = colAvailability,
                                        colStatus = colStatus,
                                        colActions = colActions,
                                        onView = { onViewTeam(team.id) },
                                        onEdit = { onEditTeam(team.id) },
                                        onToggleStatus = {
                                            val isTeamActive = team.operationalStatus.equals("Active", ignoreCase = true)
                                            val nextStatus = if (isTeamActive) "Inactive" else "Active"
                                            hrViewModel.updateProductionTeamStatus(
                                                id = team.id,
                                                newStatus = nextStatus,
                                                onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                                onError = { err -> DynamicIslandManager.showError(err) }
                                            )
                                        },
                                        onDelete = { teamToDelete = team }
                                    )
                                    HorizontalDivider(color = title_border)
                                }
                            }
                        }
                    }
                }
            }
            }
        }

        teamToDelete?.let { target ->
            DeleteModel(
                title = "Delete Production Team",
                message = "Are you sure you want to delete \"${target.name}\" (${target.teamCode})?",
                onDismiss = { teamToDelete = null },
                onDelete = {
                    val deleteId = target.id
                    teamToDelete = null
                    hrViewModel.deleteProductionTeam(
                        id = deleteId,
                        onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                        onError = { err -> DynamicIslandManager.showError(err) }
                    )
                }
            )
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Production Teams",
            sections = filterSections,
            onApply = { updated ->
                filterSections = updated
                val specs = updated.find { it.title == "Specialization" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
                val locs = updated.find { it.title == "Studio" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
                val statuses = updated.find { it.title == "Status" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
                val avails = updated.find { it.title == "Availability" }
                    ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()

                hrViewModel.applyProductionTeamFilters(specs, locs, statuses, avails)
            },
            onClearAll = {
                filterSections = filterSections.map { sec ->
                    sec.copy(options = sec.options.map { it.copy(isSelected = false) })
                }
                hrViewModel.clearProductionTeamFilters()
            }
        )
    }
}

@Composable
private fun ProductionKpiCard(
    label: String,
    value: String,
    footnote: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 1.4f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(tokens.iconSize * 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(tokens.extraPadding * 0.5f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
            ) {
                Text(
                    text = value,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (!badgeText.isNullOrBlank()) {
                    StatusBadge(
                        text = badgeText,
                        bgColor = greenBg,
                        textColor = greentext
                    )
                }
            }
            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
            Text(
                text = footnote,
                fontSize = tokens.caption,
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .size(width = width, height = tokens.iconSize * 1.3f)
            .padding(end = tokens.extraPadding * 0.8f),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = title_color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProductionTeamTableRow(
    team: ProductionTeamDto,
    colTeam: androidx.compose.ui.unit.Dp,
    colLead: androidx.compose.ui.unit.Dp,
    colMembers: androidx.compose.ui.unit.Dp,
    colSpecs: androidx.compose.ui.unit.Dp,
    colLocation: androidx.compose.ui.unit.Dp,
    colWorkload: androidx.compose.ui.unit.Dp,
    colAvailability: androidx.compose.ui.unit.Dp,
    colStatus: androidx.compose.ui.unit.Dp,
    colActions: androidx.compose.ui.unit.Dp,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val availability = team.availabilityStatus()
    val (availFg, availBg) = availabilityColors(availability)
    val workload = team.workloadValue()
    val members = team.activeMembers()
    val isTeamActive = team.operationalStatus.equals("Active", ignoreCase = true)
    val statusText = team.displayStatus()
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(whiteBg)
            .clickable { onView() }
            .padding(
                horizontal = tokens.screenPadding,
                vertical = tokens.extraPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Team Name / Code
        Column(
            modifier = Modifier
                .size(width = colTeam, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = team.name.ifBlank { "—" },
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = team.teamCode.ifBlank { "—" },
                fontSize = tokens.label,
                color = mutedText,
                maxLines = 1
            )
        }

        // 2. Team Lead
        Column(
            modifier = Modifier
                .size(width = colLead, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = team.teamLead?.name?.takeIf { it.isNotBlank() } ?: "Not Assigned",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = team.teamLead?.designation?.takeIf { it.isNotBlank() } ?: "Pending Lead",
                fontSize = tokens.label,
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3. Members Stack
        Row(
            modifier = Modifier
                .size(width = colMembers, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.3f)
        ) {
            if (members.isEmpty()) {
                Text("No members", fontSize = tokens.label, color = mutedText)
            } else {
                members.take(3).forEachIndexed { idx, member ->
                    val initial = member.name.trim().firstOrNull()?.uppercase() ?: "?"
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize * 1.3f)
                            .clip(CircleShape)
                            .background(if (idx % 2 == 0) primary_light else background_light_purple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }
                if (members.size > 3) {
                    Text(
                        text = "+${members.size - 3}",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
        }

        // 4. Specializations
        Box(
            modifier = Modifier
                .size(width = colSpecs, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            contentAlignment = Alignment.CenterStart
        ) {
            val specLabels = team.specializations.map { spec ->
                spec.tagLabel.ifBlank { spec.name.ifBlank { spec.code } }
            }.filter { it.isNotBlank() }

            if (specLabels.isEmpty()) {
                Text("—", fontSize = tokens.bodySmall, color = mutedText)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.3f)
                ) {
                    specLabels.take(2).forEach { label ->
                        StatusBadge(
                            text = label,
                            bgColor = primary_light,
                            textColor = Primary
                        )
                    }
                    if (specLabels.size > 2) {
                        StatusBadge(
                            text = "+${specLabels.size - 2}",
                            bgColor = light_grey,
                            textColor = TextSecondary
                        )
                    }
                }
            }
        }

        // 5. Location
        Box(
            modifier = Modifier
                .size(width = colLocation, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = team.location.ifBlank { "—" },
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 6. Workload
        Column(
            modifier = Modifier
                .size(width = colWorkload, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            verticalArrangement = Arrangement.Center
        ) {
            val fraction = (workload / 100f).coerceIn(0f, 1f)
            val barColor = when {
                workload >= 100 -> redText
                workload >= 80 -> yellowText
                else -> greentext
            }
            Text(
                text = "$workload%",
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(light_grey)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(barColor)
                )
            }
        }

        // 7. Availability
        Box(
            modifier = Modifier
                .size(width = colAvailability, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            contentAlignment = Alignment.CenterStart
        ) {
            StatusBadge(
                text = availability,
                bgColor = availBg,
                textColor = availFg
            )
        }

        // 8. Status
        Box(
            modifier = Modifier
                .size(width = colStatus, height = tokens.fieldHeight)
                .padding(end = tokens.extraPadding * 0.8f),
            contentAlignment = Alignment.CenterStart
        ) {
            val (statusFg, statusBg) = when (statusText) {
                "Active" -> Pair(greentext, greenBg)
                "Draft" -> Pair(yellowText, yellowBg)
                else -> Pair(TextSecondary, light_grey)
            }
            StatusBadge(
                text = statusText,
                bgColor = statusBg,
                textColor = statusFg
            )
        }

        // 9. Actions
        Box(
            modifier = Modifier.size(width = colActions, height = tokens.fieldHeight),
            contentAlignment = Alignment.CenterStart
        ) {
            ActionDropdownMenu(
                icon = Icons.Default.MoreVert,
                actions = listOf(
                    MenuAction(
                        label = "View",
                        onClick = onView
                    ),
                    MenuAction(
                        label = "Edit",
                        onClick = onEdit
                    ),
                    MenuAction(
                        label = "Delete",
                        textColor = redText,
                        onClick = onDelete
                    )
                )
            )
        }
    }
}