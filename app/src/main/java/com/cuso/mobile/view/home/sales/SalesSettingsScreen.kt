@file:Suppress("UNUSED_PARAMETER")

package com.cuso.mobile.view.home.sales

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.SaleState
import com.cuso.mobile.viewmodel.SalesViewModel
import kotlinx.coroutines.launch

private val PrimaryBlue = Color(0xFF3B3BF9)
private val TextDark = Color(0xFF1F2937)
private val TextMuted = Color(0xFF6B7280)
private val BorderMuted = Color(0xFFD1D5DB)

@Composable
fun SalesSettingsScreen(
    navController: NavController,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    GarmentTypeContent(onClose = onClose)
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GarmentTypeContent(
    onClose: () -> Unit = {}
) {
    val salesViewModel: SalesViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val commonCategories by salesViewModel.orgGarmentCategories.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoadingOrgGarments.collectAsStateWithLifecycle()
    val loadError by salesViewModel.orgGarmentError.collectAsStateWithLifecycle()
    val activeOrgCategoryIds by salesViewModel.activeOrgCategoryIds.collectAsStateWithLifecycle()

    val addState by salesViewModel.addGarmentState.collectAsStateWithLifecycle()
    val removeState by salesViewModel.removeGarmentState.collectAsStateWithLifecycle()
    val isAdding by salesViewModel.isAddingGarment.collectAsStateWithLifecycle()
    val isRemoving by salesViewModel.isRemovingGarment.collectAsStateWithLifecycle()

    var selectedCategoryIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var originalSelectedIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasChanges by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val maxSelections = 2

    val pendingAdds = remember { mutableStateListOf<String>() }
    val pendingRemoves = remember { mutableStateListOf<String>() }

    fun showSnackbar(message: String) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(Unit) {
        salesViewModel.fetchOrgGarmentCategories()
        salesViewModel.fetchActiveOrgGarments()
    }

    LaunchedEffect(activeOrgCategoryIds) {
        if (activeOrgCategoryIds != originalSelectedIds) {
            selectedCategoryIds = activeOrgCategoryIds
            originalSelectedIds = activeOrgCategoryIds
            hasChanges = false
        }
    }

    LaunchedEffect(addState) {
        when (val state = addState) {
            is SaleState.Success -> {
                salesViewModel.resetAddGarmentState()
                if (pendingAdds.isNotEmpty()) pendingAdds.removeAt(0)
                when {
                    pendingAdds.isNotEmpty() -> salesViewModel.addOrgGarmentCategory(pendingAdds.first())
                    pendingRemoves.isNotEmpty() -> salesViewModel.removeOrgGarmentCategory(pendingRemoves.first())
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
                showSnackbar("Failed to add: ${state.message}")
                salesViewModel.resetAddGarmentState()
                isSaving = false
                pendingAdds.clear()
                pendingRemoves.clear()
            }
            else -> {}
        }
    }

    LaunchedEffect(removeState) {
        when (val state = removeState) {
            is SaleState.Success -> {
                salesViewModel.resetRemoveGarmentState()
                if (pendingRemoves.isNotEmpty()) pendingRemoves.removeAt(0)
                when {
                    pendingRemoves.isNotEmpty() -> salesViewModel.removeOrgGarmentCategory(pendingRemoves.first())
                    pendingAdds.isNotEmpty() -> salesViewModel.addOrgGarmentCategory(pendingAdds.first())
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
                showSnackbar("Failed to remove: ${state.message}")
                salesViewModel.resetRemoveGarmentState()
                isSaving = false
                pendingRemoves.clear()
                pendingAdds.clear()
            }
            else -> {}
        }
    }

    fun saveChanges() {
        isSaving = true
        val originalSet = originalSelectedIds.toSet()
        val currentSet = selectedCategoryIds.toSet()

        val removedIds = (originalSet - currentSet).toList()
        val addedIds = (currentSet - originalSet).toList()

        pendingRemoves.clear()
        pendingAdds.clear()
        pendingRemoves.addAll(removedIds)
        pendingAdds.addAll(addedIds)

        when {
            pendingRemoves.isNotEmpty() -> salesViewModel.removeOrgGarmentCategory(pendingRemoves.first())
            pendingAdds.isNotEmpty() -> salesViewModel.addOrgGarmentCategory(pendingAdds.first())
        }
    }

    val busy = isSaving || isAdding || isRemoving

    Scaffold(
        topBar = {
            // ── Top Title Bar ──
            TitleBar(
                title = "Garment Type",
                onClose = {
                    if (!busy) onClose()
                    else showSnackbar("Please wait for operation to complete")
                }
            )
        },
        contentWindowInsets = WindowInsets(0,0,0,0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
            ) {

            // ── Breadcrumb ──
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ScreenBreadcrumb(listOf("Sales", "Setting"), onClick = {})
            }

            Spacer(Modifier.height(20.dp))

            // ── Main Content Section ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Garment Type",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Select exactly two garment categories for your sales operations",
                    fontSize = 13.5.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(20.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CirculerProgressIndicatorReuse()
                        }
                    }

                    loadError != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Error loading categories", color = Color.Red)
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        salesViewModel.fetchOrgGarmentCategories()
                                        salesViewModel.fetchActiveOrgGarments()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) { Text("Retry", color = whiteBg) }
                            }
                        }
                    }

                    commonCategories.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No garment categories found", color = TextMuted)
                        }
                    }

                    else -> {
                        // ── Garment Type Buttons Row (Pant, Shirt...) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            commonCategories.forEach { category ->
                                val commonCategoryId = category._id
                                val categoryName = category.categoryName
                                val isSelected = selectedCategoryIds.contains(commonCategoryId)
                                val disabled = !isSelected && selectedCategoryIds.size >= maxSelections

                                // ஐகான் கண்டறிதல் (Pant -> ic_pants, Shirt -> ic_shirts)
                                val iconRes = if (categoryName.contains("Pant", ignoreCase = true) ||
                                    categoryName.contains("Trouser", ignoreCase = true)
                                ) {
                                    R.drawable.ic_pants
                                } else {
                                    R.drawable.ic_shirts
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .background(whiteBg, RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isSelected) 1.8.dp else 1.dp,
                                            color = if (isSelected) PrimaryBlue else BorderMuted,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (busy) return@clickable
                                            when {
                                                isSelected -> {
                                                    selectedCategoryIds = selectedCategoryIds - commonCategoryId
                                                    hasChanges = selectedCategoryIds.toSet() != originalSelectedIds.toSet()
                                                    if (hasChanges) saveChanges()
                                                }
                                                !disabled -> {
                                                    selectedCategoryIds = selectedCategoryIds + commonCategoryId
                                                    hasChanges = selectedCategoryIds.toSet() != originalSelectedIds.toSet()
                                                    if (hasChanges) saveChanges()
                                                }
                                                else -> showSnackbar("Two selections already made")
                                            }
                                        }
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = categoryName,
                                        tint = if (isSelected) PrimaryBlue else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = categoryName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) PrimaryBlue else TextDark
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // ── Dashed Selection Status Banner ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = BorderMuted,
                                        style = Stroke(
                                            width = 1.2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                        ),
                                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedCategoryIds.size >= maxSelections)
                                    "Two selections already made"
                                else
                                    "${selectedCategoryIds.size} of $maxSelections selections made",
                                fontSize = 13.5.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}