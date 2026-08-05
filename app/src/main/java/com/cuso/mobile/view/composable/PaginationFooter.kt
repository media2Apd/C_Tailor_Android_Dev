@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.whiteBg


@Composable
fun PaginationFooter(
    currentPage: Int,
    pageSize: Int,
    totalItems: Int,
    onPageChange: (Int) -> Unit,
    onItemsPerPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    pageSizeOptions: List<Int> = listOf(10, 25, 50, 100)
) {
    var showItemsPerPageDropdown by remember { mutableStateOf(false) }
    val totalPages = maxOf(1, (totalItems + pageSize - 1) / pageSize)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            Box {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showItemsPerPageDropdown = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Settings, null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                    Text("$pageSize per page", fontSize = 13.sp, color = Color(0xFF374151))
                }
                DropdownMenu(
                    expanded = showItemsPerPageDropdown,
                    onDismissRequest = { showItemsPerPageDropdown = false },
                    containerColor = whiteBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    pageSizeOptions.forEach { count ->
                        DropdownMenuItem(
                            text = { Text("$count per page", color = Color(0xFF111827)) },
                            onClick = {
                                showItemsPerPageDropdown = false
                                onItemsPerPageChange(count)
                            }
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
                enabled = currentPage > 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = if (currentPage > 1) Color(0xFF374151) else Color(0xFFD1D5DB)
                )
            }
            Text("$currentPage - $totalPages", fontSize = 13.sp, color = Color(0xFF6B7280))
            IconButton(
                onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = if (currentPage < totalPages) Color(0xFF374151) else Color(0xFFD1D5DB)
                )
            }
        }
    }
}