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
import com.cuso.tailor.ui.theme.grey_border
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

// ── Animated Filter Page (Starts below TitleBar) ──
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

    BackHandler(enabled = state.isOpen) {
        state.close()
    }

    // Smooth Page Slide Animation below TitleBar
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
        Scaffold(
            containerColor = whiteBg,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                // Header below TitleBar: <- Filters        Clear all
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = title,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Text(
                        text = "Clear all",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF97316),
                        modifier = Modifier.clickable {
                            currentSections = currentSections.map { section ->
                                section.copy(options = section.options.map { it.copy(isSelected = false) })
                            }
                            onClearAll()
                        }
                    )
                }
            },
            bottomBar = {
                // Bottom Fixed Primary Button: Apply Filters
                Surface(
                    color = whiteBg,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                    ) {
                        Button(
                            onClick = {
                                onApply(currentSections)
                                state.close()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Apply Filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Search box
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                            .border(1.dp, grey_border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1E293B)),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search filters...", fontSize = 13.sp, color = Color(0xFF9CA3AF))
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

                // Filter options
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
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(Modifier.height(10.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            section.options.forEach { option ->
                                val isSelected = option.isSelected

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) Primary else Color(0xFFF1F5F9))
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
                                        .padding(horizontal = 18.dp, vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF475569),
                                        letterSpacing = 0.5.sp
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
    }
}

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