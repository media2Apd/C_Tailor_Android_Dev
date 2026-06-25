// com/cuso/mobile/view/home/SalesSettingsScreen.kt

package com.cuso.mobile.view.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.viewmodel.SaleState
import com.cuso.mobile.viewmodel.SalesViewModel
import kotlinx.coroutines.launch

@Suppress("unused")
@Composable
fun SalesSettingsScreen(
    navController: NavController,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    GarmentTypeContent(
        onClose = onClose,
        onMenuClick = onMenuClick
    )
}

// ─────────────────────────────────────────────────────────────
// GarmentTypeContent
//
// Two API calls on load:
//   1. fetchOrgGarmentCategories()  → common categories → grid tiles
//   2. fetchActiveOrgGarments()     → org garments → which tiles highlight
//
// Highlight rule:
//   commonCategory._id ∈ activeOrgCategoryIds  →  blue border + check icon
//
// Save logic (matches React GarmentTypePage.handleSave):
//   added   = selectedIds - originalIds  → addOrgGarmentCategory() one by one
//   removed = originalIds - selectedIds  → removeOrgGarmentCategory() one by one
// ─────────────────────────────────────────────────────────────
@Composable
fun GarmentTypeContent(
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ── Streams ──
    // Grid display tiles (all common categories)
    val commonCategories by salesViewModel.orgGarmentCategories.collectAsStateWithLifecycle()
    val isLoading        by salesViewModel.isLoadingOrgGarments.collectAsStateWithLifecycle()
    val loadError        by salesViewModel.orgGarmentError.collectAsStateWithLifecycle()

    // Active IDs from org garments (categoryId._id of isActive==true items)
    val activeOrgCategoryIds by salesViewModel.activeOrgCategoryIds.collectAsStateWithLifecycle()

    val addState    by salesViewModel.addGarmentState.collectAsStateWithLifecycle()
    val removeState by salesViewModel.removeGarmentState.collectAsStateWithLifecycle()
    val isAdding    by salesViewModel.isAddingGarment.collectAsStateWithLifecycle()
    val isRemoving  by salesViewModel.isRemovingGarment.collectAsStateWithLifecycle()

    // ── UI State ──
    var selectedCategoryIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var originalSelectedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasChanges          by remember { mutableStateOf(false) }
    var isSaving            by remember { mutableStateOf(false) }
    val maxSelections = 2

    var pendingAdds    by remember { mutableStateOf<MutableList<String>>(mutableListOf()) }
    var pendingRemoves by remember { mutableStateOf<MutableList<String>>(mutableListOf()) }

    fun showSnackbar(message: String) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    // ── Step 1: Load both APIs ──
    LaunchedEffect(Unit) {
        salesViewModel.fetchOrgGarmentCategories() // common categories for grid
        salesViewModel.fetchActiveOrgGarments()    // active IDs for highlight
    }

    // ── Step 2: When active IDs load, sync selection state ──
    // React equivalent:
    //   const ids = res.data.data.categories
    //     .filter(c => c.isActive).map(c => c.categoryId._id)
    //   setSelected(ids); setOriginalSelected(ids)
    LaunchedEffect(activeOrgCategoryIds) {
        if (activeOrgCategoryIds != originalSelectedIds) {
            selectedCategoryIds = activeOrgCategoryIds
            originalSelectedIds = activeOrgCategoryIds
            hasChanges = false
            Log.d("GarmentType", "✅ Synced selection: $activeOrgCategoryIds")
        }
    }

    // ── Handle Add success/failure ──
    LaunchedEffect(addState) {
        when (val state = addState) {
            is SaleState.Success -> {
                Log.d("GarmentType", "✅ Add successful")
                salesViewModel.resetAddGarmentState()
                if (pendingAdds.isNotEmpty()) pendingAdds.removeAt(0)

                when {
                    pendingAdds.isNotEmpty() ->
                        salesViewModel.addOrgGarmentCategory(pendingAdds.first())
                    pendingRemoves.isNotEmpty() ->
                        salesViewModel.removeOrgGarmentCategory(pendingRemoves.first())
                    else -> {
                        isSaving = false
                        salesViewModel.fetchActiveOrgGarments()
                        salesViewModel.fetchOrgGarmentCategories()
                        hasChanges = false
                        showSnackbar("Categories updated successfully")
                    }
                }
            }
            is SaleState.Error -> {
                Log.e("GarmentType", "❌ Add failed: ${state.message}")
                showSnackbar("Failed to add: ${state.message}")
                salesViewModel.resetAddGarmentState()
                isSaving = false
                pendingAdds.clear()
                pendingRemoves.clear()
                salesViewModel.fetchActiveOrgGarments()
                salesViewModel.fetchOrgGarmentCategories()
            }
            else -> {}
        }
    }

    // ── Handle Remove success/failure ──
    LaunchedEffect(removeState) {
        when (val state = removeState) {
            is SaleState.Success -> {
                Log.d("GarmentType", "✅ Remove successful")
                salesViewModel.resetRemoveGarmentState()
                if (pendingRemoves.isNotEmpty()) pendingRemoves.removeAt(0)

                when {
                    pendingRemoves.isNotEmpty() ->
                        salesViewModel.removeOrgGarmentCategory(pendingRemoves.first())
                    pendingAdds.isNotEmpty() ->
                        salesViewModel.addOrgGarmentCategory(pendingAdds.first())
                    else -> {
                        isSaving = false
                        salesViewModel.fetchActiveOrgGarments()
                        salesViewModel.fetchOrgGarmentCategories()
                        hasChanges = false
                        showSnackbar("Categories updated successfully")
                    }
                }
            }
            is SaleState.Error -> {
                Log.e("GarmentType", "❌ Remove failed: ${state.message}")
                showSnackbar("Failed to remove: ${state.message}")
                salesViewModel.resetRemoveGarmentState()
                isSaving = false
                pendingRemoves.clear()
                pendingAdds.clear()
                salesViewModel.fetchActiveOrgGarments()
                salesViewModel.fetchOrgGarmentCategories()
            }
            else -> {}
        }
    }

    // ── Save (matches React handleSave) ──
    // React:
    //   const added   = selected.filter(id => !originalSelected.includes(id))
    //   const removed = originalSelected.filter(id => !selected.includes(id))
    //   addCategories(added); removed.map(id => removeOneCategory(id))
    fun saveChanges() {
        isSaving = true

        val originalSet = originalSelectedIds.toSet()
        val currentSet  = selectedCategoryIds.toSet()

        val removedIds = (originalSet - currentSet).toList()
        val addedIds   = (currentSet - originalSet).toList()

        Log.d("GarmentType", "📝 To remove: $removedIds")
        Log.d("GarmentType", "📝 To add:    $addedIds")

        pendingRemoves.clear()
        pendingAdds.clear()
        pendingRemoves.addAll(removedIds)
        pendingAdds.addAll(addedIds)

        when {
            pendingRemoves.isNotEmpty() ->
                salesViewModel.removeOrgGarmentCategory(pendingRemoves.first())
            pendingAdds.isNotEmpty() ->
                salesViewModel.addOrgGarmentCategory(pendingAdds.first())
        }
    }

    val busy = isSaving || isAdding || isRemoving

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            if (!busy) onClose()
                            else showSnackbar("Please wait for operation to complete")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(22.dp),
                        tint = Color.Black
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Garment Type",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Select exactly two garment categories for your sales operations",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Save button — enabled only when hasChanges && !busy
                // React: <BaseButton disabled={saving} onClick={handleSave} />
                TextButton(
                    onClick = { saveChanges() },
                    enabled = !busy && hasChanges
                ) {
                    if (busy) {
                        Text(
                            "Saving...",
                            color = Color(0xFFAAAAAA),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            "Save",
                            color = if (hasChanges) Color(0xFF3B3BF9) else Color(0xFFAAAAAA),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF2F2F2))

            // ── Content ──
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
                loadError != null -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error loading categories", color = Color.Red)
                            Text(loadError ?: "Unknown error", color = Color.Gray, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    salesViewModel.fetchOrgGarmentCategories()
                                    salesViewModel.fetchActiveOrgGarments()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3B3BF9)
                                )
                            ) { Text("Retry", color = Color.White) }
                        }
                    }
                }
                commonCategories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("No garment categories found", color = Color(0xFF6B7280)) }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(commonCategories) { category ->
                            // ─────────────────────────────────────────────
                            // Highlight rule:
                            //   category._id  ∈  activeOrgCategoryIds
                            //   activeOrgCategoryIds = getOrgGarments
                            //     .filter { isActive }
                            //     .map { categoryId?._id }
                            //
                            // React equivalent:
                            //   isSelected = selected.includes(item._id)
                            //   selected   = orgGarments.filter(isActive).map(categoryId._id)
                            // ─────────────────────────────────────────────
                            val commonCategoryId = category._id
                            val categoryName     = category.categoryName
                            val isSelected       = selectedCategoryIds.contains(commonCategoryId)
                            val disabled         = !isSelected && selectedCategoryIds.size >= maxSelections

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF3B3BF9)
                                        else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (busy) return@clickable
                                        when {
                                            isSelected -> {
                                                // deselect
                                                selectedCategoryIds =
                                                    selectedCategoryIds - commonCategoryId
                                                hasChanges =
                                                    selectedCategoryIds.toSet() != originalSelectedIds.toSet()
                                            }
                                            !disabled -> {
                                                // select
                                                selectedCategoryIds =
                                                    selectedCategoryIds + commonCategoryId
                                                hasChanges =
                                                    selectedCategoryIds.toSet() != originalSelectedIds.toSet()
                                            }
                                            else -> showSnackbar("Two selections already made")
                                        }
                                    }
                                    .padding(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = categoryName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Medium
                                        else FontWeight.Normal,
                                        color = if (disabled) Color(0xFFAAAAAA)
                                        else Color(0xFF111827)
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(20.dp)
                                            .background(Color(0xFF3B3BF9), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
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
}