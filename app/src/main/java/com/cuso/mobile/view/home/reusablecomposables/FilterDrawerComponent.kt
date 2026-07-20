package com.cuso.mobile.view.home.reusablecomposables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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

// ── Animation timing — enter/exit share the same duration so slide + fade +
// scrim all finish together instead of one lagging behind the other. ──
private const val DrawerAnimDurationMs = 320

// ── Full-Screen Filters Page ──
@Suppress("UNUSED_PARAMETER")

@Composable
fun FilterDrawer(
    modifier: Modifier = Modifier,
    state: FilterDrawerState,
    title: String = "Filters",
    sections: List<FilterSection>,
    onApply: (List<FilterSection>) -> Unit,
    onClearAll: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    var currentSections by remember { mutableStateOf(sections) }
    var searchQuery by remember { mutableStateOf("") }

    // Track expand/collapse state per section title
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

    // ✅ Animation state — SINGLE Transition drives both the scrim and the
    // panel slide/fade. Previously the scrim used its own separate
    // rememberTransition(visibleState) while AnimatedVisibility(visibleState=...)
    // used its own internal Transition — both reading/writing the SAME
    // MutableTransitionState. Compose doesn't support two Transitions sharing
    // one MutableTransitionState cleanly, so the scrim's currentState updates
    // got interrupted/out of sync with the panel's, which is what caused the
    // "jerky, not smooth" fade instead of a clean gradual transparent fade.
    // ✅ MutableTransitionState starts at `false` — so the FIRST time state.isOpen
// becomes true, currentState(false) != targetState(true), and the transition
// actually animates instead of snapping instantly to the open state.
    val visibleState = remember { MutableTransitionState(false) }
    LaunchedEffect(state.isOpen) {
        visibleState.targetState = state.isOpen
    }
    val transition = rememberTransition(visibleState, label = "drawerTransition")

    val scrimAlpha by transition.animateFloat(
        transitionSpec = { tween(DrawerAnimDurationMs) },
        label = "scrimAlpha"
    ) { open -> if (open) 0.4f else 0f }

    BackHandler(enabled = state.isOpen) { state.close() }


// Keep Dialog composed while opening OR while close animation is still running
    if (!visibleState.isIdle || visibleState.currentState) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(20f)   // ✅ ensures it draws above everything else in the same Box
                .background(Color.Black.copy(alpha = scrimAlpha))
        ) {
            transition.AnimatedVisibility(
                visible = { it },
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(DrawerAnimDurationMs, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(DrawerAnimDurationMs)),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(DrawerAnimDurationMs, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(DrawerAnimDurationMs))
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFf8f9ff)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                            // ── Header: back arrow + title + Reset ──
                            Box(Modifier.background(Color.White).fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                            tint = Color.Black,
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clickable { state.close() }
                                        )
                                        Text(
                                            title,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                    Text(
                                        "Reset",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
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
                                Spacer(Modifier.padding(bottom = 20.dp))
                            }

                            HorizontalDivider(color = Color(0xFFF0F0F0))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // ── Search filters box ──
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF374151)),
                                            decorationBox = { inner ->
                                                if (searchQuery.isEmpty()) {
                                                    Text("Search filters...", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                                                }
                                                inner()
                                            }
                                        )
                                    }
                                }

                                // Custom content if provided
                                content?.let {
                                    item { Column { it() } }
                                }

                                // ── Dynamic sections ──
                                items(displayedSections.size) { index ->
                                    val section = displayedSections[index]
                                    val isExpanded = expandedMap[section.title] ?: true

                                    FilterSectionCard(
                                        section = section,
                                        isExpanded = isExpanded,
                                        onToggleExpand = {
                                            expandedMap[section.title] = !isExpanded
                                        },
                                        onOptionToggle = { optionId ->
                                            currentSections = currentSections.map { sec ->
                                                if (sec.title == section.title) {
                                                    val updatedOptions = if (sec.isMultiSelect) {
                                                        sec.options.map { option ->
                                                            if (option.id == optionId) option.copy(isSelected = !option.isSelected) else option
                                                        }
                                                    } else {
                                                        sec.options.map { option -> option.copy(isSelected = option.id == optionId) }
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

                            // ── Bottom Apply Bar ──
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { state.close() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel", color = Color(0xFF374151))
                                }
                                Button(
                                    onClick = {
                                        onApply(currentSections)
                                        state.close()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Check,null)
                                }
                            }
                        }
                    }
                } // AnimatedVisibility close
            } // scrim Box close

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
                Icon(section.icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
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
            .fillMaxWidth()  // ✅ Added: Makes chip take full width
            .background(if (option.isSelected) Primary else Color(0xFFF9FAFB))
            .border(
                width = if (option.isSelected) 1.5.dp else 1.dp,
                color = if (option.isSelected) Primary else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(8.dp)  // ✅ Changed: No rounded corners
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            option.label,
            fontSize = 13.sp,
            fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (option.isSelected) Color.White else Color(0xFF374151)
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
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .size(10.dp)
                )
                Text(
                    option.label,
                    fontSize = 14.sp,
                    fontWeight = if (option.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (option.isSelected) Primary else Color.Black
                )
            }
        }
    }
}

// ── Source / Garments — wrapped chip row ──
@Composable
private fun ChipRowBody(section: FilterSection, onOptionToggle: (String) -> Unit, showMore: Boolean, showCheckbox: Boolean) {
    Column {
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))  // ✅ Changed from 20.dp to 0.dp
                        .background(if (option.isSelected) Color(0xFFEEF2FF) else Color.White)
                        .border(
                            width = if (option.isSelected) 1.5.dp else 1.dp,
                            color = if (option.isSelected) Primary else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(8.dp)  // ✅ Changed from 20.dp to 0.dp
                        )
                        .clickable { onOptionToggle(option.id) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✅ Garments always shows a checkbox glyph (checked/unchecked). Source shows none.
                    if (showCheckbox) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (option.isSelected) Color(0xFF6366F1) else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (option.isSelected) Color(0xFF6366F1) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(3.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (option.isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
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
                modifier = Modifier.clickable { /* TODO: expand full garment list */ }
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
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
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
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
            .clickable { /* TODO: open picker */ }
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
                    .background(if (option.isSelected) Color(0xFFEEF2FF) else Color.White)
                    .border(
                        width = if (option.isSelected) 1.5.dp else 1.dp,
                        color = if (option.isSelected) Color(0xFF6366F1) else Color(0xFFE5E7EB),
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
        border = if (option.isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            option.label,
            fontSize = 13.sp,
            fontWeight = if (option.isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (option.isSelected) Color.White else Color(0xFF475569),
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
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    showFilterIcon: Boolean = true,
    onFilterClick: (() -> Unit)? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = DefaultBorderGray,
    textSecondaryColor: Color = DefaultTextSecondary,
    height: androidx.compose.ui.unit.Dp = 52.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder, color = textSecondaryColor) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = textSecondaryColor)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = textSecondaryColor,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQueryChange("") }
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = borderColor,
                focusedBorderColor = accentColor,
                focusedContainerColor=Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (showFilterIcon) {
            Box(
                modifier = Modifier
                    .size(height)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = onFilterClick != null) { onFilterClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF111827))
            }
        }
    }
}