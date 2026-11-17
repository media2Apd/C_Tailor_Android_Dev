@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.safety_stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.SafetyStockItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

private data class HealthTheme(
    val textColor: Color,
    val bgColor: Color,
    val progressColor: Color
)

private fun getHealthTheme(status: String): HealthTheme {
    return when (status.trim().lowercase()) {
        "safe", "healthy" -> HealthTheme(
            textColor = greentext,
            bgColor = greenBg,
            progressColor = Primary
        )
        "low", "warning" -> HealthTheme(
            textColor = yellowText,
            bgColor = yellowBg,
            progressColor = yellowText
        )
        "critical", "out of stock" -> HealthTheme(
            textColor = redText,
            bgColor = redBg,
            progressColor = redText
        )
        else -> HealthTheme(
            textColor = Primary,
            bgColor = light_blue,
            progressColor = Primary
        )
    }
}

@Composable
fun SafetyStockScreen(
    onClose: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val safetyStockPagingState by viewModel.safetyStockPagingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.safetyStockError.collectAsStateWithLifecycle()

    var isInitialized by remember { mutableStateOf(false) }

    // Initial load and debounced search trigger
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchSafetyStock()
        } else {
            delay(400)
            viewModel.fetchSafetyStock(search = searchQuery.trim())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(
                    title = "Safety Stock",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Stock Items...",
                        accentColor = BluePrimary,
                        borderColor = BorderGray,
                        textSecondaryColor = TextSecondary,
                        onFilterClick = onFilterClick
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when {
                            errorMessage != null && safetyStockPagingState.items.isEmpty() && !safetyStockPagingState.isInitialLoading -> {
                                AppErrorState(
                                    title = "Failed to load safety stock",
                                    message = errorMessage ?: "Something went wrong. Please check your connection.",
                                    onRetry = { viewModel.fetchSafetyStock(search = searchQuery.trim()) }
                                )
                            }

                            !safetyStockPagingState.isInitialLoading && safetyStockPagingState.items.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.isBlank()) "No safety stock records found." else "No matching items.",
                                        color = mutedText,
                                        fontSize = tokens.bodyMedium
                                    )
                                }
                            }

                            else -> {
                                PaginatedDataList(
                                    state = safetyStockPagingState,
                                    onLoadMore = { viewModel.loadMoreSafetyStock(search = searchQuery.trim()) },
                                    modifier = Modifier.fillMaxSize()
                                ) { item ->
                                    val theme = getHealthTheme(item.health)
                                    val displayUnit = item.unit?.ifBlank { "Meters" } ?: "Meters"
                                    val progressRatio = (item.healthPercent.toFloat() / 100f).coerceIn(0.08f, 1f)

                                    val cardActions = listOf(
                                        MenuAction("Edit") { onEditClick(item.itemId) }
                                    )

                                    DataCard(
                                        item = item,
                                        title = item.name.ifBlank { "Product" },
                                        subtitle = buildString {
                                            append("SKU: ${item.sku.ifBlank { "—" }}")
                                            if (!item.variantLabel.isNullOrBlank()) {
                                                append(" · Variant: ${item.variantLabel}")
                                            }
                                        },
                                        headerContent = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.warehouseName ?: "Main Warehouse",
                                                    fontSize = tokens.bodySmall,
                                                    color = headerGrey
                                                )

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(theme.bgColor)
                                                            .padding(horizontal = 10.dp, vertical = 0.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = item.health.ifBlank { "-" },
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.Medium,
                                                            color = theme.textColor
                                                        )
                                                    }

                                                    ActionDropdownMenu(
                                                        icon = Icons.Default.MoreVert,
                                                        actions = cardActions
                                                    )
                                                }
                                            }
                                        },
                                        content = {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Current Stock",
                                                            fontSize = tokens.label,
                                                            color = mutedText
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = "${item.available.toInt()} $displayUnit",
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextPrimary
                                                        )
                                                    }

                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "Health Ratio",
                                                            fontSize = tokens.label,
                                                            color = mutedText
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = "${item.healthPercent}%",
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = theme.textColor
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(8.dp))

                                                DataCardProgressBar(
                                                    progress = progressRatio,
                                                    progressColor = theme.progressColor,
                                                    trackColor = dividerColor
                                                )

                                                Spacer(Modifier.height(14.dp))
                                                HorizontalDivider(color = grey_border, thickness = 1.dp)
                                                Spacer(Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Safety Level",
                                                            fontSize = tokens.label,
                                                            color = mutedText
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = "${item.safetyStock.toInt()} $displayUnit",
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.Medium,
                                                            color = TextPrimary
                                                        )
                                                    }

                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "Reorder Level",
                                                            fontSize = tokens.label,
                                                            color = mutedText
                                                        )
                                                        Spacer(Modifier.height(2.dp))
                                                        Text(
                                                            text = "${item.reorderLevel.toInt()} $displayUnit",
                                                            fontSize = tokens.bodySmall,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyStockMetricsGrid(items: List<SafetyStockItemDto>) {
    val tokens = LocalAppTokens.current

    val totalItems = items.size
    val safeCount = items.count { it.health.trim().lowercase() in listOf("safe", "healthy") }
    val lowCount = items.count { it.health.trim().lowercase() in listOf("low", "warning") }
    val criticalCount = items.count { it.health.trim().lowercase() in listOf("critical", "out of stock") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SafetyStockMetricCard(label = "Monitored Items", value = "$totalItems", modifier = Modifier.weight(1f))
            SafetyStockMetricCard(label = "Safe Stock", value = "$safeCount", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SafetyStockMetricCard(label = "Low Warning", value = "$lowCount", modifier = Modifier.weight(1f))
            SafetyStockMetricCard(label = "Critical Reorder", value = "$criticalCount", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SafetyStockMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = headerGrey,
                maxLines = 1
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}