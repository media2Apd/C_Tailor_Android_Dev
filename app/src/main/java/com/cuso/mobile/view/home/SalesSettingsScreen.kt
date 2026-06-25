package com.cuso.mobile.view.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.viewmodel.SaleState
import com.cuso.mobile.viewmodel.SalesViewModel

// ─────────────────────────────────────────────────────────────
// Sales Settings Screen
// ─────────────────────────────────────────────────────────────
@Composable
fun SalesSettingsScreen(
    navController: NavController,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Simple Header - Only burger menu + close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 30.dp, end = 30.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Menu Button - Triggers TopNavBar burger menu
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(lightGray, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Garment Type",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            // Close Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(lightGray, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(22.dp),
                    tint = Color.Black
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF2F2F2))

        GarmentTypeContent(
            onSaveComplete = onClose
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Garment Type Content - With Save Button
// ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────
// Garment Type Content - With Org Garment API
// ─────────────────────────────────────────────────────────────
@Composable
fun GarmentTypeContent(
    onSaveComplete: () -> Unit = {}
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val context = LocalContext.current

    val orgGarmentCategories by salesViewModel.orgGarmentCategories.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingOrgGarments.collectAsStateWithLifecycle()
    val error by salesViewModel.orgGarmentError.collectAsStateWithLifecycle()
    val addState by salesViewModel.addGarmentState.collectAsStateWithLifecycle()
    val removeState by salesViewModel.removeGarmentState.collectAsStateWithLifecycle()
    val isAdding by salesViewModel.isAddingGarment.collectAsStateWithLifecycle()
    val isRemoving by salesViewModel.isRemovingGarment.collectAsStateWithLifecycle()

    var selectedCount by remember { mutableIntStateOf(0) }
    val maxSelections = 2
    var selectedGarmentIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var originalSelectedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasChanges by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        salesViewModel.fetchOrgGarmentCategories()
    }

    LaunchedEffect(orgGarmentCategories) {
        if (orgGarmentCategories.isNotEmpty()) {
            val activeIds = orgGarmentCategories.map { it._id }
            originalSelectedIds = activeIds
            selectedGarmentIds = activeIds
            selectedCount = activeIds.size
            hasChanges = false
        }
    }

    // Handle Add success
    LaunchedEffect(addState) {
        when (val state = addState) {
            is SaleState.Success -> {
                Toast.makeText(context, "Category added successfully!", Toast.LENGTH_SHORT).show()
                salesViewModel.resetAddGarmentState()
                isSaving = false
                onSaveComplete()
            }
            is SaleState.Error -> {
                Toast.makeText(context, "Failed to add: ${state.message}", Toast.LENGTH_LONG).show()
                salesViewModel.resetAddGarmentState()
                isSaving = false
            }
            else -> {}
        }
    }

    // Handle Remove success
    LaunchedEffect(removeState) {
        when (val state = removeState) {
            is SaleState.Success -> {
                Toast.makeText(context, "Category removed successfully!", Toast.LENGTH_SHORT).show()
                salesViewModel.resetRemoveGarmentState()
                isSaving = false
                onSaveComplete()
            }
            is SaleState.Error -> {
                Toast.makeText(context, "Failed to remove: ${state.message}", Toast.LENGTH_LONG).show()
                salesViewModel.resetRemoveGarmentState()
                isSaving = false
            }
            else -> {}
        }
    }

    fun saveChanges() {
        if (!hasChanges) return

        isSaving = true

        val originalSet = originalSelectedIds.toSet()
        val currentSet = selectedGarmentIds.toSet()

        val removedIds = originalSet - currentSet
        val addedIds = currentSet - originalSet

        removedIds.forEach { categoryId ->
            salesViewModel.removeOrgGarmentCategory(categoryId)
        }

        addedIds.forEach { categoryId ->
            salesViewModel.addOrgGarmentCategory(categoryId)
        }

        if (removedIds.isEmpty() && addedIds.isEmpty()) {
            isSaving = false
            onSaveComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Garment Type",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Select exactly two garment categories for your sales operations",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $error", color = Color.Red)
            }
        } else if (orgGarmentCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No garment categories found", color = Color(0xFF6B7280))
            }
        } else {
            orgGarmentCategories.forEach { category ->
                // ✅ Direct access - categoryName is at root level
                val categoryName = category.categoryName
                val categoryId = category._id
                val isSelected = selectedGarmentIds.contains(categoryId)

                val imageRes = when (categoryName.lowercase()) {
                    "shirt" -> R.drawable.pant
                    "pant" -> R.drawable.pant
                    else -> R.drawable.pant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSelected) Color(0xFFF5F5FF) else Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) Color(0xFF3B3BF9) else Color(0xFFE5E7EB),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (!isSaving && !isAdding && !isRemoving) {
                                if (isSelected) {
                                    selectedGarmentIds = selectedGarmentIds - categoryId
                                    selectedCount = selectedGarmentIds.size
                                    hasChanges = true
                                } else if (selectedCount < maxSelections) {
                                    selectedGarmentIds = selectedGarmentIds + categoryId
                                    selectedCount = selectedGarmentIds.size
                                    hasChanges = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Maximum 2 selections allowed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = categoryName,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(Modifier.width(16.dp))

                    Text(
                        text = categoryName,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF111827) else Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF3B3BF9),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        // Save Button
        if (hasChanges && !isLoading) {
            Button(
                onClick = { saveChanges() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = !isAdding && !isRemoving && !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B3BF9)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isAdding || isRemoving || isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...", color = Color.White)
                } else {
                    Text("Save Changes", color = Color.White)
                }
            }
        }

        Text(
            text = if (selectedCount >= maxSelections) {
                "Two selections already made"
            } else {
                "${maxSelections - selectedCount} more selection${if (maxSelections - selectedCount > 1) "s" else ""} remaining"
            },
            fontSize = 14.sp,
            color = if (selectedCount >= maxSelections) Color(0xFF3B3BF9) else Color(0xFF6B7280),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}