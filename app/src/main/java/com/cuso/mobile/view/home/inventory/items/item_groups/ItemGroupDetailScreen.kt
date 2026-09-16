@file:Suppress("unused")
package com.cuso.mobile.view.home.inventory.items.item_groups

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.activity_green_bg
import com.cuso.mobile.ui.theme.darkGreenBg
import com.cuso.mobile.ui.theme.iconMuted
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.sectionBorder
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ImageUploadSection
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.cuso.mobile.R
import com.cuso.mobile.ui.theme.title_color

@Composable
fun ItemGroupDetailScreen(
    itemGroupId: String,
    viewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onAdjustStock: () -> Unit = {},
    onExportPdf: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Transactions

    val uploadedImages = remember { mutableStateListOf<Any>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uploadedImages.addAll(uris)
        }
    }

    LaunchedEffect(itemGroupId) {
        viewModel.fetchItemGroupViewOne(itemGroupId) {}
        settingsViewModel.fetchProductCategories()
    }

    val detail by viewModel.selectedItemGroupDetail.collectAsState()
    val isLoadingDetail by viewModel.isLoadingItemGroupDetail.collectAsState()
    val productCategories by settingsViewModel.productCategories.collectAsState()

    // Match Category ID with Category Name
    val categoryDisplayName = remember(detail?.categoryId, productCategories) {
        val catId = detail?.categoryId
        if (!catId.isNullOrBlank()) {
            productCategories.find { it.id == catId }?.name ?: catId
        } else {
            "N/A"
        }
    }

    // Populate existing images from API if available
    LaunchedEffect(detail?.media?.images) {
        val serverImages = detail?.media?.images?.map { it.fileUrl } ?: emptyList()
        if (serverImages.isNotEmpty() && uploadedImages.isEmpty()) {
            uploadedImages.addAll(serverImages)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar("Item Group Detail", onClose = onClose)
            }
        }
    ) { innerPadding ->
        if (isLoadingDetail && detail == null) {
            ListSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Title and Action Buttons (Edit, PDF)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detail?.name?.ifBlank { "Cotton Twil" } ?: "Cotton Twil",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEdit(itemGroupId) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onExportPdf,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                                    contentDescription = "Export PDF",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Adjust Stock Button
                item {
                    Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                        Button(
                            onClick = onAdjustStock,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(
                                text = "Adjust Stock",
                                color = whiteBg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Tab Bar (Overview | Transactions)
                item {
                    Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(whiteBg, RoundedCornerShape(8.dp))
                                .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            // Overview Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedTab == 0) primary_light else Color.Transparent)
                                    .clickable { selectedTab = 0 },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Layers,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) Primary else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Overview",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedTab == 0) Primary else Color(0xFF64748B)
                                    )
                                }
                            }

                            // Transactions Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedTab == 1) primary_light else Color.Transparent)
                                    .clickable { selectedTab = 1 },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = if (selectedTab == 1) Primary else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Transactions",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedTab == 1) Primary else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // Section 1: Item Group Basic Info
                    item {
                        DetailSectionCard(title = "Item Group", tokens = tokens) {
                            DetailRowItem(label = "Item Group Name", value = detail?.name?.ifBlank { "cottontwill" } ?: "cottontwill")
                            DetailRowItem(label = "SKU Prefix", value = detail?.groupCode ?: "COT")
                            DetailRowItem(label = "Category", value = categoryDisplayName)
                            DetailRowItem(label = "Brand", value = detail?.brand?.takeIf { it.isNotBlank() } ?: "N/A")
                            DetailRowItem(label = "Created Source", value = "Manual Entry")
                            DetailRowItem(
                                label = "Created On",
                                value = formatIsoDate(detail?.createdAt),
                                showDivider = false
                            )
                        }
                    }

                    // Section 2: Purchase Information
                    item {
                        val costPrice = detail?.averageCost ?: detail?.pricing?.costPrice ?: 0.0
                        DetailSectionCard(
                            title = "Purchase Information",
                            icon = Icons.Outlined.ShoppingCart,
                            tokens = tokens
                        ) {
                            DetailRowItem(label = "Average Cost", value = "₹%.2f".format(costPrice))
                            DetailRowItem(label = "Cost Range", value = "₹%.0f".format(costPrice))
                            DetailRowItem(
                                label = "Purchase Account",
                                value = "Cost of Goods Sold",
                                showDivider = false
                            )
                        }
                    }

                    // Section 3: Sales Information
                    item {
                        val sellingPrice = detail?.averageSellingPrice ?: detail?.pricing?.sellingPrice ?: 0.0
                        val costPrice = detail?.averageCost ?: detail?.pricing?.costPrice ?: 0.0
                        val margin = if (sellingPrice > 0) ((sellingPrice - costPrice) / sellingPrice) * 100 else 0.0

                        DetailSectionCard(
                            title = "Sales Information",
                            icon = Icons.Outlined.LocalOffer,
                            tokens = tokens
                        ) {
                            DetailRowItem(label = "Average Cost", value = "₹%.2f".format(sellingPrice))
                            DetailRowItem(label = "Price Range", value = "₹%.0f".format(sellingPrice))
                            DetailRowItem(
                                label = "Average Margin",
                                value = "%.1f%%".format(margin),
                                valueColor = Primary,
                                showDivider = false
                            )
                        }
                    }

                    // Section 4: Reusable Image Upload Section
                    item {
                        Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                            ImageUploadSection(
                                isImage = true,
                                selectedImages = uploadedImages.toList(),
                                onBrowseClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onCameraClick = null,
                                onRemoveImage = { itemToRemove ->
                                    uploadedImages.remove(itemToRemove)
                                },
                                browseText = "Browse Files",
                                uploadBoxHeight = 110.dp
                            )
                        }
                    }

                    // Section 5: Inventory Snapshot
                    item {
                        val totalVariants = detail?.variantCount ?: detail?.variants?.size ?: 0
                        val unitStr = detail?.unit?.takeIf { it.isNotBlank() } ?: "Pieces (Pcs)"

                        DetailSectionCard(
                            title = "Inventory Snapshot",
                            icon = Icons.Outlined.Layers,
                            tokens = tokens
                        ) {
                            DetailRowItem(
                                label = "Total Stock on Hand",
                                value = "$totalVariants $unitStr",
                                valueColor = Primary
                            )
                            DetailRowItem(
                                label = "Opening Stock",
                                value = "0 $unitStr"
                            )
                            DetailRowItem(
                                label = "Committed Stock",
                                value = "0 $unitStr"
                            )
                            DetailRowItem(
                                label = "Available for Sale",
                                value = "0 $unitStr",
                                showDivider = false
                            )
                        }
                    }

                    // Section 6: Reorder Intelligence
                    item {
                        DetailSectionCard(title = "Reorder Intelligence", tokens = tokens) {
                            Spacer(Modifier.height(10.dp))
                            // Status Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(activity_green_bg, RoundedCornerShape(8.dp))
                                    .border(1.dp, darkGreenBg.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = darkGreenBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "All Variants Are Well Stocked",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = darkGreenBg
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val unitStr = detail?.unit?.takeIf { it.isNotBlank() } ?: "Pieces (Pcs)"
                            DetailRowItem(
                                label = "Total Reorder Point",
                                value = "0 $unitStr"
                            )
                            DetailRowItem(
                                label = "Active Variants",
                                value = "${detail?.variants?.size ?: detail?.variantCount ?: 0}",
                                showDivider = false
                            )
                        }
                    }

                    // Section 7: Item Variants Empty State
                    item {
                        val variantCount = detail?.variants?.size ?: detail?.variantCount ?: 0
                        DetailSectionCard(title = "Item Variants ($variantCount)", tokens = tokens) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(primary_light, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.box),
                                            contentDescription = null,
                                            tint = Primary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No work experience added yet",
                                        fontSize = 12.sp,
                                        color = iconMuted
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Transactions Tab Content
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found",
                                fontSize = 14.sp,
                                color = iconMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SUB-COMPONENTS (FULL EDGE-TO-EDGE WHITE BACKGROUND TITLE + PADDED CONTENT)
// =============================================================================

@Composable
private fun DetailSectionCard(
    title: String,
    tokens: AppDesignTokens,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Full Edge-to-Edge White Background Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
        }

        // Section Content with Standard Screen Padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    valueColor: Color = title_color,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
        if (showDivider) {
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
        }
    }
}

private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "21 Jul 2026"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        date?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}