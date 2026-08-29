@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter", "VariableNeverRead"
)

package com.cuso.mobile.view.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cuso.mobile.ui.theme.Primary
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.unit.Dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg

// ── Filter Section Data ──
enum class FilterSectionType {
    CHIP_GRID,      // Date Range – 2 column grid of chips
    CHECKBOX_LIST,  // Status – vertical checkbox list
    CHIP_ROW,       // Source – wrapped chip row
    CHIP_ROW_MORE,  // Garments – wrapped chip row with "+ More"
    AMOUNT_RANGE,   // Amount Range – min / max text fields
    DROPDOWN,       // Sales Person / Location – "Select X" row
    PRIORITY_DOTS   // Priority – colored dot chips
}

data class FilterSection(
    val title: String,
    val options: List<FilterOption>,
    val isMultiSelect: Boolean = false,
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

// ── Priority colors ──
private fun priorityDotColor(id: String): Color = when (id) {
    "high" -> Color(0xFFEF4444)
    "medium" -> Color(0xFFF59E0B)
    "low" -> Color(0xFF22C55E)
    else -> Color(0xFF9CA3AF)
}

private const val FilterHalfFraction = 0.55f
private const val FilterFullFraction = 0.96f

@Suppress("UNUSED_PARAMETER")
@Composable
fun FilterDrawer(
    modifier: Modifier = Modifier,
    state: FilterDrawerState,
    title: String = "Filters",
    sections: List<FilterSection>,
    onApply: (List<FilterSection>) -> Unit,
    onClearAll: () -> Unit,
    onBackgroundBlurChange: (Dp) -> Unit = {},
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    var currentSections by remember { mutableStateOf(sections) }
    var searchQuery by remember { mutableStateOf("") }

    //  Blur state for background
    var filterDrawerBlur by remember { mutableStateOf(0.dp) }

    //  Sheet state for SmoothBottomSheet
    var sheetState by remember { mutableStateOf(SheetValue.Hidden) }

    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(sections) {
        currentSections = sections
        sections.forEach { sec ->
            if (!expandedMap.containsKey(sec.title)) {
                expandedMap[sec.title] = true
            }
        }
    }

    val displayedSections = if (searchQuery.isBlank()) currentSections
    else currentSections.filter { it.title.contains(searchQuery, ignoreCase = true) }

    val scope = rememberCoroutineScope()

    //  Handle sheet open/close based on state
    LaunchedEffect(state.isOpen) {
        if (state.isOpen) {
            sheetState = SheetValue.Collapsed
            searchQuery = ""
        } else {
            sheetState = SheetValue.Hidden
        }
    }

    BackHandler(enabled = state.isOpen) { state.close() }

    // ── Scrim — fade (kept for compatibility, but SmoothBottomSheet handles its own scrim) ──
    AnimatedVisibility(
        visible = state.isOpen && sheetState == SheetValue.Hidden,
        enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)),
        modifier = modifier.fillMaxSize().zIndex(20f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(blackTitle.copy(alpha = 0.35f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { state.close() }
        )
    }

    // ── SmoothBottomSheet instead of custom slide animation ──
    SmoothBottomSheet(
        state = sheetState,
        onStateChange = { newState ->
            sheetState = newState
            if (newState == SheetValue.Hidden) {
                state.close()
            }
        },
        collapsedFraction = FilterHalfFraction,
        expandedFraction = FilterFullFraction,
        dragCloseEnabled = true,
        scrollableContent = false,
        sheetBackgroundColor = Primary_background,
        onDismissRequest = { state.close() },
        onBlurScrimChange = { blur, _ ->
            filterDrawerBlur = blur
            onBackgroundBlurChange(blur)
        }
    ) {
        // ── Sheet Content ──
        Column(modifier = Modifier.fillMaxSize()) {


            // ── Header: back/close + title + Reset ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = blackTitle,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { state.close() }
                    )
                    Text(
                        title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = blackTitle
                    )
                }
                Text(
                    "Reset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = blackTitle,
                    modifier = Modifier.clickable {
                        currentSections = currentSections.map { section ->
                            section.copy(
                                options = section.options.map { it.copy(isSelected = false) },
                                minAmount = "",
                                maxAmount = "",
                                dropdownValue = ""
                            )
                        }
                        onClearAll()
                    }
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = title_border)

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .background(whiteBg, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF374151)
                                ),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Search filters...",
                                            fontSize = 14.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                    inner()
                                }
                            )
                        }
                    }

                    content?.let {
                        item { Column { it() } }
                    }

                    items(displayedSections.size) { index ->
                        val section = displayedSections[index]
                        val isExpanded = expandedMap[section.title] ?: true

                        FilterSectionCard(
                            section = section,
                            isExpanded = isExpanded,
                            onToggleExpand = { expandedMap[section.title] = !isExpanded },
                            onOptionToggle = { optionId ->
                                currentSections = currentSections.map { sec ->
                                    if (sec.title == section.title) {
                                        val updatedOptions = if (sec.isMultiSelect) {
                                            sec.options.map { option ->
                                                if (option.id == optionId) option.copy(
                                                    isSelected = !option.isSelected
                                                ) else option
                                            }
                                        } else {
                                            sec.options.map { option ->
                                                option.copy(
                                                    isSelected = option.id == optionId
                                                )
                                            }
                                        }
                                        sec.copy(options = updatedOptions)
                                    } else sec
                                }
                            },
                            onMinAmountChange = { value ->
                                currentSections = currentSections.map { sec ->
                                    if (sec.title == section.title) sec.copy(minAmount = value) else sec
                                }
                            },
                            onMaxAmountChange = { value ->
                                currentSections = currentSections.map { sec ->
                                    if (sec.title == section.title) sec.copy(maxAmount = value) else sec
                                }
                            }
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
                StepNavigationFab(
                    showBack = true,
                    backLabel = "Cancel",
                    onBack = { state.close() },
                    showBackArrow = false, // Optional: usually Cancel doesn't need an arrow
                    trailingAction = TrailingFabAction.Update(
                        label = "Apply ",
                        onClick = {
                            onApply(currentSections)
                            state.close()
                        }
                    ),
                    // Adjusting fractions to give them equal space
                    backWidthFraction = 0.25f,
                    trailingWidthFraction = 0.25f
                )
            }

//            HorizontalDivider(color = title_border)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(whiteBg)
//                    .padding(horizontal = 16.dp, vertical = 12.dp),
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                OutlinedButton(
//                    onClick = { state.close() },
//                    modifier = Modifier.weight(1f),
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    Text("Cancel", color = Color(0xFF374151))
//                }
//                Button(
//                    onClick = {
//                        onApply(currentSections)
//                        state.close()
//                    },
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    Icon(Icons.Default.Check, null)
//                }
//            }
        }
    }
}

// ── Section Card: header row (icon + title + chevron) + expandable body ──
@Composable
private fun FilterSectionCard(
    section: FilterSection,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOptionToggle: (String) -> Unit,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Header row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(section.icon, contentDescription = null, tint = blackTitle, modifier = Modifier.size(18.dp))
                Text(section.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            }
            Icon(
                if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(10.dp))
            when (section.type) {
                FilterSectionType.CHECKBOX_LIST -> CheckboxListBody(section, onOptionToggle)
                FilterSectionType.CHIP_ROW -> ChipRowBody(section, onOptionToggle, showMore = false, showCheckbox = false)
                FilterSectionType.CHIP_ROW_MORE -> ChipRowBody(section, onOptionToggle, showMore = true, showCheckbox = true)
                FilterSectionType.AMOUNT_RANGE -> AmountRangeBody(section, onMinAmountChange, onMaxAmountChange)
                FilterSectionType.DROPDOWN -> DropdownBody(section)
                FilterSectionType.PRIORITY_DOTS -> PriorityDotsBody(section, onOptionToggle)
                FilterSectionType.CHIP_GRID -> ChipGridBody(section, onOptionToggle)
            }
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = Color(0xFFF3F4F6))
    }
}

// ── Date Range — 2 column grid ──
@Composable
private fun ChipGridBody(section: FilterSection, onOptionToggle: (String) -> Unit) {
    val rows = section.options.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowOptions.forEach { option ->
                    GridChip(option = option, modifier = Modifier.weight(1f)) { onOptionToggle(option.id) }
                }
                if (rowOptions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GridChip(option: FilterOption, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .background(if (option.isSelected) Primary else Color(0xFFF9FAFB))
            .border(
                width = if (option.isSelected) 1.5.dp else 1.dp,
                color = if (option.isSelected) Primary else grey_border,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            option.label,
            fontSize = 13.sp,
            fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (option.isSelected) whiteBg else Color(0xFF374151)
        )
    }
}

// ── Status — vertical checkbox list ──
@Composable
private fun CheckboxListBody(section: FilterSection, onOptionToggle: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        section.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionToggle(option.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = option.isSelected,
                    onCheckedChange = { onOptionToggle(option.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Primary,
                        uncheckedColor = Color(0xFFCBD5E1),
                        checkmarkColor = whiteBg
                    ),
                    modifier = Modifier
                        .size(10.dp)
                )
                Text(
                    option.label,
                    fontSize = 14.sp,
                    fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (option.isSelected) Primary else blackTitle
                )
            }
        }
    }
}

// ── Source / Garments — wrapped chip row ──
@Composable
private fun ChipRowBody(section: FilterSection, onOptionToggle: (String) -> Unit, showMore: Boolean, showCheckbox: Boolean) {
    Column {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (option.isSelected) primary_light else whiteBg)
                        .border(
                            width = if (option.isSelected) 1.5.dp else 1.dp,
                            color = if (option.isSelected) Primary else grey_border,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onOptionToggle(option.id) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showCheckbox) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (option.isSelected) Color(0xFF6366F1) else whiteBg)
                                .border(
                                    width = 1.dp,
                                    color = if (option.isSelected) Color(0xFF6366F1) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(3.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (option.isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = whiteBg, modifier = Modifier.size(10.dp))
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        option.label,
                        fontSize = 13.sp,
                        fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (option.isSelected) Color(0xFF6366F1) else Color(0xFF374151)
                    )
                }
            }
        }
        if (showMore) {
            Spacer(Modifier.height(8.dp))
            Text(
                "+ More",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Primary,
                modifier = Modifier.clickable {  }
            )
        }
    }
}

// ── Amount Range — min / max fields ──
@Composable
private fun AmountRangeBody(
    section: FilterSection,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AmountField(
            value = section.minAmount,
            placeholder = "Min Amount",
            onValueChange = onMinAmountChange,
            modifier = Modifier.weight(1f)
        )
        Text("To", fontSize = 13.sp, color = Color(0xFF9CA3AF))
        AmountField(
            value = section.maxAmount,
            placeholder = "Max Amount",
            onValueChange = onMaxAmountChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AmountField(value: String, placeholder: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(46.dp)
            .background(whiteBg, RoundedCornerShape(10.dp))
            .border(1.dp, grey_border, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = whiteBg, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF374151)),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, fontSize = 13.sp, color = Color(0xFF9CA3AF))
                inner()
            }
        )
    }
}

// ── Sales Person / Location — dropdown-style row ──
@Composable
private fun DropdownBody(section: FilterSection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(whiteBg, RoundedCornerShape(10.dp))
            .border(1.dp, grey_border, RoundedCornerShape(10.dp))
            .clickable {  }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            section.dropdownValue.ifEmpty { "Select ${section.title}" },
            fontSize = 13.sp,
            color = if (section.dropdownValue.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF374151)
        )
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
    }
}

// ── Priority — colored dot chips ──
@Composable
private fun PriorityDotsBody(section: FilterSection, onOptionToggle: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        section.options.forEach { option ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (option.isSelected) primary_light else whiteBg)
                    .border(
                        width = if (option.isSelected) 1.5.dp else 1.dp,
                        color = if (option.isSelected) Color(0xFF6366F1) else grey_border,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onOptionToggle(option.id) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(priorityDotColor(option.id))
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    option.label,
                    fontSize = 13.sp,
                    fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (option.isSelected) Color(0xFF6366F1) else Color(0xFF374151)
                )
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")

// ── Filter Chip (kept for backward-compat where referenced elsewhere) ──
@Composable
fun FilterChip(
    option: FilterOption,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (option.isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9),
        border = if (option.isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            option.label,
            fontSize = 13.sp,
            fontWeight = if (option.isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (option.isSelected) whiteBg else Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private val DefaultBorderGray = Color(0xFFE8E8ED)
private val DefaultTextSecondary = Color(0xFF9A9AA8)

/**
 * Reusable Search + Filter bar.
 * Use anywhere: pass searchQuery + onQueryChange, optionally show filter icon.
 *
 * Example:
 * SearchFilterBar(
 *     query = searchQuery,
 *     onQueryChange = { searchQuery = it },
 *     placeholder = "Search Customers...",
 *     onFilterClick = { /* open filter drawer */ }
 * )
 */
@Composable
fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    showFilterIcon: Boolean = true,
    onFilterClick: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = DefaultBorderGray,
    textSecondaryColor: Color = DefaultTextSecondary,
    height: Dp = 44.dp
) {
    val tokens = LocalAppTokens.current
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 10.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(height),
                decorationBox = { innerTextField ->

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                whiteBg,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                borderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = textSecondaryColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {

                            if (query.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 14.sp,
                                    color = textSecondaryColor
                                )
                            }

                            innerTextField()
                        }

                        if (query.isNotEmpty()) {

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = textSecondaryColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        onQueryChange("")
                                    }
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.width(10.dp))

            if (showFilterIcon) {

                Box(
                    modifier = Modifier
                        .size(height)
                        .clip(RoundedCornerShape(12.dp))
                        .background(whiteBg)
                        .border(
                            1.dp,
                            borderColor,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = onFilterClick != null) {
                            onFilterClick?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color(0xFF111827),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}