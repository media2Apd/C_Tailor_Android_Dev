@file:Suppress("UNUSED_PARAMETER", "unused", "unusedVariable")

package com.cuso.mobile.view.home.sales.settings.garment

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.settings.SegmentItem
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.dashedBorder
import com.cuso.mobile.viewmodel.SettingsViewModel

@Composable
fun SalesSettingsScreen(
    navController: NavController? = null,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onAddSegmentClick: () -> Unit = {},
    onEditSegmentClick: (SegmentItem) -> Unit = {},
    onAddGarmentClick: () -> Unit = {},
    onConfigureGarmentClick: (segmentId: String, garmentId: String, garmentTitle: String) -> Unit = { _, _, _ -> },
    viewModel: SettingsViewModel = hiltViewModel()
) {
    GarmentTypeContent(
        onClose = onClose,
        onAddSegmentClick = onAddSegmentClick,
        onEditSegmentClick = onEditSegmentClick,
        onAddGarmentClick = onAddGarmentClick,
        onConfigureGarmentClick = onConfigureGarmentClick,
        viewModel = viewModel
    )
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GarmentTypeContent(
    onClose: () -> Unit = {},
    onAddSegmentClick: () -> Unit = {},
    onEditSegmentClick: (SegmentItem) -> Unit = {},
    onAddGarmentClick: () -> Unit = {},
    onConfigureGarmentClick: (segmentId: String, garmentId: String, garmentTitle: String) -> Unit = { _, _, _ -> },
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val segments by viewModel.segments.collectAsState()
    val segmentsError by viewModel.segmentsError.collectAsState()
    val isLoadingSegments by viewModel.isLoadingSegments.collectAsState()

    val garments by viewModel.garments.collectAsState()
    val garmentsError by viewModel.garmentsError.collectAsState()
    val isLoadingGarments by viewModel.isLoadingGarments.collectAsState()

    var selectedSegmentIndex by remember { mutableIntStateOf(0) }
    var menuExpandedSegmentId by remember { mutableStateOf<String?>(null) }
    var segmentToDelete by remember { mutableStateOf<SegmentItem?>(null) }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchSegments()
        viewModel.fetchGarments()
    }

    LaunchedEffect(segmentsError) {
        if (!segmentsError.isNullOrBlank()) {
            errorMessage = segmentsError
        }
    }

    LaunchedEffect(garmentsError) {
        if (!garmentsError.isNullOrBlank()) {
            errorMessage = garmentsError
        }
    }

    // Currently Selected Segment
    val selectedSegment = if (segments.isNotEmpty()) {
        val safeIndex = selectedSegmentIndex.coerceIn(0, segments.size - 1)
        segments[safeIndex]
    } else null

    // Filter garments based on applicableSegments array matching the selected segment
    val filteredGarments = remember(garments, selectedSegment, searchQuery) {
        garments.filter { garment ->
            val matchesSegment = if (selectedSegment == null) true else {
                garment.applicableSegments.any { it.id == selectedSegment.id }
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                garment.name.contains(searchQuery, ignoreCase = true) ||
                        (garment.displayName?.contains(searchQuery, ignoreCase = true) == true)
            }
            matchesSegment && matchesSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Garment",
                icon = Icons.Default.Add,
                onClick = onAddGarmentClick,
                bottomPadding = 40.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                TitleBar(
                    title = "Garments",
                    onClose = onClose
                )

                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Garments...",
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )

                if (segments.isNotEmpty()) {
                    val safeIndex = selectedSegmentIndex.coerceIn(0, segments.size - 1)

                    ScrollableTabRow(
                        selectedTabIndex = safeIndex,
                        edgePadding = tokens.screenPadding,
                        containerColor = whiteBg,
                        divider = { HorizontalDivider(color = title_border, thickness = 1.dp) },
                        indicator = { tabPositions ->
                            if (safeIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
                                    color = Primary,
                                    height = 2.5.dp
                                )
                            }
                        }
                    ) {
                        segments.forEachIndexed { index, segmentItem ->
                            val isSelected = safeIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedSegmentIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = segmentItem.name,
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Primary else TextSecondary
                                        )

                                        if (isSelected) {
                                            Spacer(Modifier.width(4.dp))
                                            Box {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clickable(
                                                            indication = null,
                                                            interactionSource = remember { MutableInteractionSource() }
                                                        ) {
                                                            menuExpandedSegmentId = segmentItem.id
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.MoreVert,
                                                        contentDescription = "Segment Options",
                                                        tint = Primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = menuExpandedSegmentId == segmentItem.id,
                                                    onDismissRequest = { menuExpandedSegmentId = null },
                                                    containerColor = whiteBg
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "Edit",
                                                                fontSize = tokens.bodyMedium,
                                                                color = title_color
                                                            )
                                                        },
                                                        onClick = {
                                                            menuExpandedSegmentId = null
                                                            onEditSegmentClick(segmentItem)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "Delete",
                                                                fontSize = tokens.bodyMedium,
                                                                color = redText
                                                            )
                                                        },
                                                        onClick = {
                                                            menuExpandedSegmentId = null
                                                            segmentToDelete = segmentItem
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                                .dashedBorder(
                                    color = Primary,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    strokeWidth = 1.2.dp,
                                    cornerRadius = tokens.cardCornerRadius * 0.5f
                                )
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { onAddSegmentClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Add Segment",
                                    color = Primary,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (filteredGarments.isEmpty() && !isLoadingGarments) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No garments found in this segment",
                                    fontSize = tokens.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = filteredGarments,
                            key = { _, item -> item.id }
                        ) { _, item ->
                            val configuredStatus = if (item.measurementFields.isNotEmpty()) "CONFIGURED" else "NOT STARTED"
                            val fieldsCount = item.measurementFields.size
                            val measurementsCount = item.measurementFields.count { it.isRequired }

                            // Inside GarmentTypeContent LazyColumn items:
                            GarmentCategoryCard(
                                title = item.displayName ?: item.name,
                                subtitle = "${item.measurementFields.size} Fields",
                                status = if (item.measurementFields.isNotEmpty()) "CONFIGURED" else "NOT STARTED",
                                iconRes = R.drawable.ic_shirts,
                                onConfigureClick = {
                                    // Ensure selectedSegment and item.id are not null
                                    val segId = selectedSegment?.id ?: ""
                                    val garmId = item.id
                                    val title = "${selectedSegment?.name.orEmpty()} ${item.displayName ?: item.name}".trim()

                                    Log.d("NAV_PARAM", "Clicked segmentId: $segId, garmentId: $garmId")
                                    onConfigureGarmentClick(segId, garmId, title)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        segmentToDelete?.let { segment ->
            AlertDialog(
                onDismissRequest = { segmentToDelete = null },
                containerColor = whiteBg,
                title = {
                    Text(
                        text = "Delete Segment",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete the \"${segment.name}\" segment?",
                        fontSize = tokens.bodyMedium,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idToDelete = segment.id
                            segmentToDelete = null
                            viewModel.deleteSegment(
                                id = idToDelete,
                                onSuccess = { msg ->
                                    selectedSegmentIndex = 0
                                    successMessage = msg
                                },
                                onError = { err ->
                                    errorMessage = ErrorMapper.map(err)
                                }
                            )
                        }
                    ) {
                        Text(
                            text = "Delete",
                            color = redText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { segmentToDelete = null }) {
                        Text(
                            text = "Cancel",
                            color = TextSecondary
                        )
                    }
                }
            )
        }

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
fun GarmentCategoryCard(
    title: String,
    subtitle: String,
    status: String,
    iconRes: Int,
    onConfigureClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.fieldHeight)
                        .background(background_light_purple, RoundedCornerShape(tokens.cardCornerRadius * 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.width(8.dp))

                CategoryStatusBadge(status = status)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onConfigureClick() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure →",
                    color = Primary,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CategoryStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor) = when (status.uppercase()) {
        "CONFIGURED" -> Pair(greenBg, darkGreenBg)
        "PENDING" -> Pair(yellowBg, yellowText)
        else -> Pair(primary_light, Primary)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = tokens.label,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}