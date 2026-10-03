@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter",
    "VariableNeverRead"
)

package com.cuso.tailor.view.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg

// ── Filter Section Data ──
enum class FilterSectionType {
    CHIP_GRID,
    CHECKBOX_LIST,
    CHIP_ROW,
    CHIP_ROW_MORE,
    AMOUNT_RANGE,
    DROPDOWN,
    PRIORITY_DOTS
}

data class FilterSection(
    val title: String,
    val options: List<FilterOption>,
    val isMultiSelect: Boolean = true,
    val type: FilterSectionType = FilterSectionType.CHIP_ROW,
    val icon: ImageVector = Icons.Filled.Sell,
    val minAmount: String = "",
    val maxAmount: String = "",
    val dropdownValue: String = ""
)

data class FilterOption(
    val id: String,
    val label: String,
    val isSelected: Boolean = false
)

// ── Default Lead Filter Options from Reference ──
fun getDefaultLeadFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Date Range",
        options = listOf(
            FilterOption("today", "Today"),
            FilterOption("this_week", "This Week"),
            FilterOption("this_month", "This Month"),
            FilterOption("custom", "Custom")
        ),
        isMultiSelect = false
    ),
    FilterSection(
        title = "Lead Status",
        options = listOf(
            FilterOption("new", "New"),
            FilterOption("scheduled", "Scheduled"),
            FilterOption("in_progress", "In Progress"),
            FilterOption("contacted", "Contacted"),
            FilterOption("qualified", "Qualified"),
            FilterOption("not_qualified", "Not Qualified"),
            FilterOption("lost", "Lost"),
            FilterOption("junk", "Junk")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Priority",
        options = listOf(
            FilterOption("low", "Low"),
            FilterOption("medium", "Medium"),
            FilterOption("high", "High"),
            FilterOption("urgent", "Urgent")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Source",
        options = listOf(
            FilterOption("walk_in", "Walk-in"),
            FilterOption("instagram", "Instagram"),
            FilterOption("facebook", "Facebook"),
            FilterOption("google", "Google"),
            FilterOption("referral", "Referral"),
            FilterOption("website", "Website"),
            FilterOption("other", "Other")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Enquiry Type",
        options = listOf(
            FilterOption("new_order", "New Order"),
            FilterOption("alteration", "Alteration"),
            FilterOption("repair", "Repair"),
            FilterOption("other", "Other")
        ),
        isMultiSelect = true
    )
)

// ── Filter Drawer State ──
@Stable
interface FilterDrawerState {
    val isOpen: Boolean
    fun open()
    fun close()
    fun toggle()
}

class FilterDrawerStateImpl : FilterDrawerState {
    override var isOpen by mutableStateOf(false)
        private set

    override fun open() { isOpen = true }
    override fun close() { isOpen = false }
    override fun toggle() { isOpen = !isOpen }
}

@Composable
fun rememberFilterDrawerState(): FilterDrawerState {
    return remember { FilterDrawerStateImpl() }
}

// ── Animated Filter Page with CSS Glassmorphism Effect ──
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDrawer(
    modifier: Modifier = Modifier,
    state: FilterDrawerState,
    title: String = "Filters",
    sections: List<FilterSection> = emptyList(),
    onApply: (List<FilterSection>) -> Unit,
    onClearAll: () -> Unit,
    onBackgroundBlurChange: (Dp) -> Unit = {},
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val tokens = LocalAppTokens.current

    var currentSections by remember(sections) {
        val base = if (sections.isEmpty()) getDefaultLeadFilterSections() else sections
        mutableStateOf(base)
    }

    var searchQuery by remember { mutableStateOf("") }

    // Intercept hardware back press when drawer is open
    BackHandler(enabled = state.isOpen) {
        state.close()
    }

    // Trigger backdrop-filter: blur(20px) on parent screen
    LaunchedEffect(state.isOpen) {
        onBackgroundBlurChange(if (state.isOpen) 20.dp else 0.dp)
    }

    // ── CSS Glassmorphism Brushes ──
    val glassCardBackground = Brush.linearGradient(
        colors = listOf(
            whiteBg.copy(alpha = 0.88f),
            whiteBg.copy(alpha = 0.72f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            whiteBg.copy(alpha = 0.70f),
            whiteBg.copy(alpha = 0.20f),
            whiteBg.copy(alpha = 0.40f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    // Smooth page slide animation with glass backdrop
    AnimatedVisibility(
        visible = state.isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(260)),
        modifier = modifier
            .fillMaxSize()
            .zIndex(50f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(whiteBg) // Backdrop shadow depth
        ) {
            Scaffold(
                containerColor = whiteBg,
                contentWindowInsets = WindowInsets(0),
                modifier = Modifier
                    .fillMaxSize()
                    .background(glassCardBackground)
                    .border(
                        width = 1.dp,
                        brush = glassBorderBrush,
                        shape = RoundedCornerShape(topStart = tokens.cardCornerRadius, bottomStart = tokens.cardCornerRadius)
                    ),
                topBar = {
                    // Glass Header with spec highlight
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteBg.copy(alpha = 0.35f))
                                .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { state.close() }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = title_color,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Text(
                                    text = title,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }

                            Text(
                                text = "Clear all",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = orangeText,
                                modifier = Modifier.clickable {
                                    currentSections = currentSections.map { section ->
                                        section.copy(options = section.options.map { it.copy(isSelected = false) })
                                    }
                                    onClearAll()
                                }
                            )
                        }

                        // Bottom hairline divider for glass header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.BottomCenter)
                                .background(whiteBg.copy(alpha = 0.3f))
                        )
                    }
                },
                bottomBar = {
                    // Glass Bottom Fixed Action Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg.copy(alpha = 0.45f))
                    ) {
                        // Top horizontal highlight (CSS ::before equivalent)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            whiteBg.copy(alpha = 0.8f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
                        ) {
                            Button(
                                onClick = {
                                    onApply(currentSections)
                                    state.close()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(tokens.cardCornerRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.buttonHeight)
                            ) {
                                Text(
                                    text = "Apply Filters",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Primary_background)
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Glass Search Box
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.fieldHeight)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                .background(whiteBg.copy(alpha = 0.4f))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            whiteBg.copy(alpha = 0.8f),
                                            whiteBg.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                                )
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                ),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search filters...",
                                                fontSize = tokens.bodySmall,
                                                color = mutedText,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        }
                    }

                    content?.let {
                        item { Column { it() } }
                    }

                    // Filter Categories and Options
                    val filteredSections = if (searchQuery.isBlank()) {
                        currentSections
                    } else {
                        currentSections.mapNotNull { sec ->
                            val matchingOptions = sec.options.filter { it.label.contains(searchQuery, ignoreCase = true) }
                            if (sec.title.contains(searchQuery, ignoreCase = true) || matchingOptions.isNotEmpty()) {
                                sec.copy(options = if (matchingOptions.isNotEmpty()) matchingOptions else sec.options)
                            } else null
                        }
                    }

                    items(filteredSections.size) { index ->
                        val section = filteredSections[index]

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = section.title,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )

                            Spacer(Modifier.height(10.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                section.options.forEach { option ->
                                    val isSelected = option.isSelected

                                    // Glass Option Pills with CSS border specular reflection
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                            .background(
                                                if (isSelected) Primary
                                                else primary_light
                                            )
                                            .border(
                                                width = 1.dp,
                                                brush = if (isSelected) {
                                                    SolidColor(Primary)
                                                } else {
                                                    Brush.linearGradient(
                                                        listOf(
                                                            whiteBg.copy(alpha = 0.7f),
                                                            whiteBg.copy(alpha = 0.15f)
                                                        )
                                                    )
                                                },
                                                shape = RoundedCornerShape(tokens.cardCornerRadius)
                                            )
                                            .clickable {
                                                currentSections = currentSections.map { sec ->
                                                    if (sec.title == section.title) {
                                                        val updatedOptions = if (sec.isMultiSelect) {
                                                            sec.options.map { opt ->
                                                                if (opt.id == option.id) opt.copy(isSelected = !opt.isSelected)
                                                                else opt
                                                            }
                                                        } else {
                                                            sec.options.map { opt ->
                                                                opt.copy(isSelected = opt.id == option.id)
                                                            }
                                                        }
                                                        sec.copy(options = updatedOptions)
                                                    } else sec
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option.label.toTitleCase(),
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) whiteBg else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // ── Top Specular Light Highlight (CSS ::before replica) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                whiteBg.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // ── Left Specular Light Highlight (CSS ::after replica) ──
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                whiteBg.copy(alpha = 0.85f),
                                Color.Transparent,
                                whiteBg.copy(alpha = 0.30f)
                            )
                        )
                    )
            )
        }
    }
}
fun getDefaultOpportunityFilterSections(
    stages: List<String> = emptyList(),
    categories: List<String> = emptyList()
): List<FilterSection> = listOf(
    FilterSection(
        title = "Deal Stage",
        options = if (stages.isNotEmpty()) {
            stages.map { FilterOption(id = it, label = it) }
        } else {
            listOf(
                FilterOption("new_opportunity", "New Opportunity"),
                FilterOption("qualification", "Qualification"),
                FilterOption("proposal", "Proposal/Quotation"),
                FilterOption("negotiation", "Negotiation"),
                FilterOption("closed_won", "Closed Won"),
                FilterOption("closed_lost", "Closed Lost")
            )
        },
        isMultiSelect = true
    ),
    FilterSection(
        title = "Customer Type",
        options = listOf(
            FilterOption("retail", "Retail Customer"),
            FilterOption("wholesale", "Wholesale Customer"),
            FilterOption("corporate", "Corporate Customer"),
            FilterOption("boutique", "Boutique / Reseller")
        ),
        isMultiSelect = true
    ),
    FilterSection(
        title = "Product Category",
        options = if (categories.isNotEmpty()) {
            categories.map { FilterOption(id = it, label = it) }
        } else {
            listOf(
                FilterOption("bridal_blouse", "Bridal Blouse")
            )
        },
        isMultiSelect = true
    )
)

private val DefaultBorderGray = Color(0xFFE8E8ED)
private val DefaultTextSecondary = Color(0xFF9A9AA8)

/**
 * Reusable Search and Filter bar with matching height (40.dp) and filter count badge.
 */
@Composable
fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    isSearchBarAlone: Boolean = false,
    showFilterIcon: Boolean = true,
    filterCount: Int = 0,
    onFilterClick: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = DefaultBorderGray,
    textSecondaryColor: Color = DefaultTextSecondary,
    height: Dp = 40.dp,
    isDropdownExpanded: Boolean = false,
    onDismissDropdown: () -> Unit = {},
    dropdownContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val tokens = LocalAppTokens.current
    val shouldShowFilter = !isSearchBarAlone && showFilterIcon

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Box Container
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = whiteBg,
                        shape = if (isDropdownExpanded && dropdownContent != null) {
                            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        } else {
                            RoundedCornerShape(12.dp)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDropdownExpanded) accentColor else borderColor,
                        shape = if (isDropdownExpanded && dropdownContent != null) {
                            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        } else {
                            RoundedCornerShape(12.dp)
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = textSecondaryColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(accentColor),
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF111827)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (query.isEmpty()) {
                                    Text(
                                        text = placeholder,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = textSecondaryColor
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (query.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = textSecondaryColor,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onQueryChange("") }
                        )
                    }
                }

                // Dropdown Content (Rendered only when expanded)
                if (isDropdownExpanded && dropdownContent != null) {
                    HorizontalDivider(
                        color = borderColor.copy(alpha = 0.8f),
                        thickness = 1.dp
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                        color = whiteBg
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            dropdownContent()
                        }
                    }
                }
            }
        }

        // Filter Button with Badge
        if (shouldShowFilter) {
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(height)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(whiteBg)
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = onFilterClick != null) {
                            onFilterClick?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Notification badge showing selected filter count
                if (filterCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-5).dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .zIndex(2f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterCount > 99) "99+" else filterCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }
    }
}