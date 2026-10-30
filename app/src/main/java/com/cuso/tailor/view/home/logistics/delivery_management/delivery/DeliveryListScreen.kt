package com.cuso.tailor.view.home.logistics.delivery_management.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.MemberItem
import com.cuso.tailor.model.logistics.DeliveryItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun DeliveryListScreen(
    viewModel: LogisticsViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onViewDeliveryDetail: (String) -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val deliveries by viewModel.filteredDeliveries.collectAsState()
    val searchQuery by viewModel.deliverySearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingDeliveries.collectAsState()
    val selectedStatus by viewModel.selectedDeliveryStatus.collectAsState()
    val selectedType by viewModel.selectedDeliveryType.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedDelivery by remember { mutableStateOf<DeliveryItemDto?>(null) }

    val filterSections = remember(selectedStatus, selectedType) {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("ready", "Ready", selectedStatus?.equals("Ready", true) == true),
                    FilterOption("in_transit", "In Transit", selectedStatus?.equals("In Transit", true) == true),
                    FilterOption("delivered", "Delivered", selectedStatus?.equals("Delivered", true) == true),
                    FilterOption("assigned", "Assigned", selectedStatus?.equals("Assigned", true) == true)
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Delivery Type",
                options = listOf(
                    FilterOption("chennai", "Chennai", selectedType?.equals("Chennai", true) == true),
                    FilterOption("outstation", "Outstation", selectedType?.equals("Outstation", true) == true)
                ),
                isMultiSelect = false
            )
        )
    }

    val activeFilterCount = (if (!selectedStatus.isNullOrBlank()) 1 else 0) +
            (if (!selectedType.isNullOrBlank()) 1 else 0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0,0,0,0),
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "All Delivery",
                    onClose = onNavigateBack
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Transparent)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onDeliverySearchQueryChange(it) },
                    placeholder = "Search SO number, customer...",
                    showFilterIcon = true,
                    filterCount = activeFilterCount,
                    onFilterClick = { filterDrawerState.open() },
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = mutedText,
                    height = tokens.fieldHeight
                )

                when {
                    isLoading && deliveries.isEmpty() -> {
                        ListSkeleton()
                    }

                    deliveries.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No deliveries found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = deliveries,
                                key = { it.id }
                            ) { deliveryItem ->
                                DeliveryDataCardItem(
                                    item = deliveryItem,
                                    formattedDate = viewModel.formatDeliveryDate(deliveryItem.deliveryDate),
                                    onAssignClick = {
                                        selectedDelivery = deliveryItem
                                        showAssignDialog = true
                                    },
                                    onViewDetailClick = { validId ->
                                        onViewDeliveryDetail(validId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            title = "Filters",
            sections = filterSections,
            onApply = { updatedSections ->
                val statusOption = updatedSections
                    .find { it.title == "Status" }
                    ?.options
                    ?.find { it.isSelected }

                val typeOption = updatedSections
                    .find { it.title == "Delivery Type" }
                    ?.options
                    ?.find { it.isSelected }

                viewModel.onDeliveryStatusFilterChange(statusOption?.label)
                viewModel.onDeliveryTypeFilterChange(typeOption?.label)
            },
            onClearAll = {
                viewModel.clearDeliveryFilters()
            }
        )

        if (showAssignDialog && selectedDelivery != null) {
            AssignDeliveryDialog(
                hrViewModel = hrViewModel,
                onDismiss = {
                    showAssignDialog = false
                    selectedDelivery = null
                },
                onConfirm = { member, targetDate, instructions ->
                    selectedDelivery?.let { delivery ->
                        val dateParts = targetDate.split("-")
                        val apiDate = if (dateParts.size == 3) {
                            "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
                        } else {
                            targetDate
                        }

                        viewModel.assignDelivery(
                            deliveryId = delivery.id,
                            staffId = member._id,
                            staffInstructions = instructions,
                            targetCompletionDate = apiDate,
                            onSuccess = {
                                showAssignDialog = false
                                selectedDelivery = null
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DeliveryDataCardItem(
    item: DeliveryItemDto,
    formattedDate: String,
    onAssignClick: () -> Unit,
    onViewDetailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    val badgeBgColor = if (item.status.equals("In Transit", ignoreCase = true)) {
        orangeBg
    } else {
        greenBg
    }

    val badgeTextColor = if (item.status.equals("In Transit", ignoreCase = true)) {
        orangeText
    } else {
        greentext
    }

    // Resolve non-null, non-blank MongoDB ID (_id) or fallback to deliveryId
    val targetId = remember(item.id, item.deliveryId) {
        item.id.trim().ifBlank { item.deliveryId.trim() }
    }

    DataCard(
        item = item,
        modifier = modifier,
        title = item.customer,
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        topBadgeText = item.status,
        topBadgeInline = true,
        topBadgeShowDot = false,
        topBadgeBgColor = badgeBgColor,
        topBadgeTextColor = badgeTextColor,
        topBadgeCornerRadius = tokens.cardCornerRadius,
        // Clicking card itself navigates to detail safely
        onClick = {
            if (targetId.isNotBlank()) {
                onViewDetailClick(targetId)
            }
        },
        actions = listOf(
            MenuAction(
                label = "View Details",
                textColor = title_color,
                onClick = {
                    if (targetId.isNotBlank()) {
                        onViewDetailClick(targetId)
                    }
                }
            )
        ),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(PanelBg)
                    .border(
                        width = 1.dp,
                        color = sectionBorder,
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    )
                    .padding(
                        horizontal = tokens.extraPadding,
                        vertical = tokens.extraPadding
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = "Courier Partner",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = item.courierPartner ?: item.deliveryId,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = "Delivery Type",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = item.deliveryType,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = "Order ID",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = item.orderId.removePrefix("ORD-2026-"),
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Tracking Number : ",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )

                    Text(
                        text = formattedDate,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onAssignClick,
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = whiteBg
                    ),
                    contentPadding = PaddingValues(
                        horizontal = tokens.extraPadding * 1.5f,
                        vertical = 0.dp
                    ),
                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                ) {
                    Text(
                        text = item.action,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    )
}

@Composable
fun AssignDeliveryDialog(
    hrViewModel: HrViewModel,
    onDismiss: () -> Unit,
    onConfirm: (MemberItem, String, String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val members by hrViewModel.members.collectAsState()
    val isLoading by hrViewModel.isLoadingMembers.collectAsState()
    val membersError by hrViewModel.membersError.collectAsState()

    var selectedStaff by remember {
        mutableStateOf("")
    }

    var targetDate by remember {
        mutableStateOf("")
    }

    var instructions by remember {
        mutableStateOf("")
    }

    var staffExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (members.isEmpty()) {
            hrViewModel.fetchMembers(
                page = 1,
                limit = 100
            )
        }
    }

    val staffNameToMember = remember(members) {
        members.associateBy { member ->
            "${member.firstName.orEmpty()} ${member.lastName.orEmpty()}".trim()
        }
    }

    val staffOptions = remember(staffNameToMember) {
        staffNameToMember.keys.filter { it.isNotBlank() }
    }

    val selectedMember = staffNameToMember[selectedStaff]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(
                if (tokens.isTablet) 0.72f else 0.96f
            ),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            shadowElevation = tokens.extraPadding
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = tokens.cardPadding,
                            vertical = tokens.extraPadding
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Assign Delivery",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(
                            tokens.iconSize + tokens.extraPadding
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = headerGrey,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PanelBg)
                        .padding(
                            horizontal = tokens.cardPadding,
                            vertical = tokens.extraPadding
                        )
                ) {
                    Text(
                        text = "Assigning for Delivery",
                        fontSize = tokens.bodySmall,
                        color = headerGrey
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = tokens.cardHeight * 5.5f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = tokens.cardPadding,
                            vertical = tokens.cardPadding
                        ),
                    verticalArrangement = Arrangement.spacedBy(
                        tokens.extraPadding
                    )
                ) {
                    FormDropdown(
                        label = "Select Staff",
                        value = when {
                            isLoading && staffOptions.isEmpty() ->
                                "Loading staff..."
                            selectedStaff.isNotBlank() ->
                                selectedStaff
                            else ->
                                "Select Staff"
                        },
                        expanded = staffExpanded &&
                                !isLoading &&
                                staffOptions.isNotEmpty(),
                        onExpandChange = {
                            staffExpanded = it
                        },
                        options = staffOptions,
                        onOptionSelected = { name ->
                            selectedStaff = name
                            staffExpanded = false
                        },
                        isRequired = true,
                        enabled = !isLoading && staffOptions.isNotEmpty()
                    )

                    if (membersError != null && staffOptions.isEmpty()) {
                        Text(
                            text = membersError ?: "Failed to load staff",
                            fontSize = tokens.caption,
                            color = redText
                        )

                        TextButton(
                            onClick = {
                                hrViewModel.fetchMembers(
                                    page = 1,
                                    limit = 100
                                )
                            }
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = tokens.bodySmall,
                                color = Primary
                            )
                        }
                    }

                    if (!isLoading &&
                        membersError == null &&
                        staffOptions.isEmpty()
                    ) {
                        Text(
                            text = "No staff members available",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }

                    FormLabel(
                        text = "Target Completion Date",
                        isRequired = true
                    )

                    DatePickerField(
                        value = targetDate,
                        onDateSelected = {
                            targetDate = it
                        }
                    )

                    FormTextArea(
                        value = instructions,
                        onValueChange = {
                            instructions = it
                        },
                        placeholder = "Add specific stitching or adjustment instructions...",
                        minLines = 4,
                        maxLines = 6,
                        borderColor = BorderGray,
                        focusedBorderColor = Primary,
                        textColor = TextPrimary,
                        placeholderColor = mutedText
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(
                            horizontal = tokens.cardPadding,
                            vertical = tokens.extraPadding
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            color = headerGrey
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(tokens.extraPadding)
                    )

                    Button(
                        onClick = {
                            selectedMember?.let { member ->
                                onConfirm(
                                    member,
                                    targetDate,
                                    instructions.trim()
                                )
                            }
                        },
                        enabled = selectedMember != null &&
                                targetDate.isNotBlank() &&
                                !isLoading,
                        modifier = Modifier.height(tokens.buttonHeight),
                        shape = RoundedCornerShape(
                            tokens.cardCornerRadius * 0.6f
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = whiteBg,
                            disabledContainerColor = light_grey,
                            disabledContentColor = mutedText
                        ),
                        contentPadding = PaddingValues(
                            horizontal = tokens.extraPadding * 1.5f,
                            vertical = 0.dp
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Assign Staff",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}