@file:Suppress("SpellCheckingInspection", "unused")

package com.cuso.tailor.view.home.inventory.multi_channel_management.basic_info

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import java.util.Locale
import java.util.UUID
import androidx.core.graphics.toColorInt

// ==============================================================================
// 1. DATA MODELS & STATE HOLDERS
// ==============================================================================

data class PricingTierRow(
    val id: String = UUID.randomUUID().toString(),
    val tierLabel: String,
    val minQty: String = "",
    val maxQty: String = "",
    val unitPrice: String = "",
    val discountPercent: String = "0% off"
)

data class WarehouseStockRow(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ImageVector,
    val stockQty: String = ""
)

data class ItemVariantRow(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val size: String,
    val color: String,
    val price: String = "",
    val sku: String = "",
    val stock: String = "",
    val isActive: Boolean = true
)

// ==============================================================================
// 2. MAIN WIZARD COMPOSABLE
// ==============================================================================

@Composable
fun BasicInfoScreen(
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var currentStep by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            "Basic Info",
            "Media",
            "Variants",
            "Pricing",
            "Inventory",
            "Channels",
            "SEO",
            "Preview",
            "Publish"
        )
    }

    // --- Form States: Step 1 Basic Info (All Initialized Clean/Blank) ---
    var productName by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var isAutoSku by remember { mutableStateOf(false) }

    var productType by remember { mutableStateOf("Select an option") }
    var productTypeExpanded by remember { mutableStateOf(false) }

    var category by remember { mutableStateOf("Select an option") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var subCategory by remember { mutableStateOf("Select an option") }
    var subCategoryExpanded by remember { mutableStateOf(false) }

    var brand by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var productStatus by remember { mutableStateOf("Draft") }

    var fabric by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var sleeveType by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }

    var fitType by remember { mutableStateOf("Select an option") }
    var fitTypeExpanded by remember { mutableStateOf(false) }

    var collarType by remember { mutableStateOf("Select an option") }
    var collarTypeExpanded by remember { mutableStateOf(false) }

    var shortDescription by remember { mutableStateOf("") }
    var longDescription by remember { mutableStateOf("") }
    val features = remember { mutableStateListOf<String>() }

    // --- Form States: Step 2 Media ---
    val uploadedImages = remember { mutableStateListOf<Uri>() }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uploadedImages.addAll(uris)
    }

    // --- Form States: Step 3 Variants ---
    val sizes = remember { mutableStateListOf<String>() }
    val colors = remember { mutableStateListOf<String>() }
    val generatedVariants = remember { mutableStateListOf<ItemVariantRow>() }

    // --- Form States: Step 4 Pricing ---
    var currency by remember { mutableStateOf("Select an option") }
    var currencyExpanded by remember { mutableStateOf(false) }

    var taxCategory by remember { mutableStateOf("Select an option") }
    var taxExpanded by remember { mutableStateOf(false) }

    var sellingPrice by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    val pricingTiers = remember { mutableStateListOf<PricingTierRow>() }

    // --- Form States: Step 5 Inventory ---
    var trackInventory by remember { mutableStateOf(false) }
    var openingStock by remember { mutableStateOf("") }
    var openingStockRate by remember { mutableStateOf("") }
    var reorderLevel by remember { mutableStateOf("") }
    var safetyStock by remember { mutableStateOf("") }
    var emailAlerts by remember { mutableStateOf(false) }
    var dashboardAlerts by remember { mutableStateOf(false) }
    val warehouses = remember { mutableStateListOf<WarehouseStockRow>() }

    // --- Form States: Step 6 Channels ---
    var onlineStoreEnabled by remember { mutableStateOf(false) }
    var retailPosEnabled by remember { mutableStateOf(false) }
    var marketplaceEnabled by remember { mutableStateOf(false) }
    var wholesaleEnabled by remember { mutableStateOf(false) }
    var onlineCustomPrice by remember { mutableStateOf("") }
    var retailCustomPrice by remember { mutableStateOf("") }

    // --- Form States: Step 7 SEO ---
    var seoTitle by remember { mutableStateOf("") }
    var metaDescription by remember { mutableStateOf("") }
    var urlSlug by remember { mutableStateOf("") }
    val searchTags = remember { mutableStateListOf<String>() }

    // --- Form States: Step 9 Publish ---
    var publishOption by remember { mutableStateOf("Save as Draft") }
    var visibilityOption by remember { mutableStateOf("Public") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = "Basic Info",
                onClose = onClose
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Step ${currentStep + 1} of 9: ${steps[currentStep]}",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = headerGrey,
                modifier = Modifier.padding(horizontal = tokens.screenPadding)
            )
            Spacer(Modifier.height(8.dp))

            AppUnderlineTabRow(
                tabs = steps,
                selectedIndex = currentStep,
                onTabSelected = { currentStep = it },
                isScrollable = true
            )
            HorizontalDivider(color = title_border)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    0 -> StepBasicInfo(
                        tokens = tokens,
                        productName = productName, onProductNameChange = { productName = it },
                        sku = sku, onSkuChange = { sku = it },
                        isAutoSku = isAutoSku, onAutoSkuChange = { isAutoSku = it },
                        productType = productType, productTypeExpanded = productTypeExpanded,
                        onProductTypeExpanded = { productTypeExpanded = it }, onProductTypeSelected = { productType = it },
                        category = category, categoryExpanded = categoryExpanded,
                        onCategoryExpanded = { categoryExpanded = it }, onCategorySelected = { category = it },
                        subCategory = subCategory, subCategoryExpanded = subCategoryExpanded,
                        onSubCategoryExpanded = { subCategoryExpanded = it }, onSubCategorySelected = { subCategory = it },
                        brand = brand, onBrandChange = { brand = it },
                        manufacturer = manufacturer, onManufacturerChange = { manufacturer = it },
                        productStatus = productStatus, onProductStatusChange = { productStatus = it },
                        fabric = fabric, onFabricChange = { fabric = it },
                        pattern = pattern, onPatternChange = { pattern = it },
                        sleeveType = sleeveType, onSleeveTypeChange = { sleeveType = it },
                        material = material, onMaterialChange = { material = it },
                        fitType = fitType, fitTypeExpanded = fitTypeExpanded,
                        onFitTypeExpanded = { fitTypeExpanded = it }, onFitTypeSelected = { fitType = it },
                        collarType = collarType, collarTypeExpanded = collarTypeExpanded,
                        onCollarTypeExpanded = { collarTypeExpanded = it }, onCollarTypeSelected = { collarType = it },
                        shortDescription = shortDescription, onShortDescriptionChange = { shortDescription = it },
                        longDescription = longDescription, onLongDescriptionChange = { longDescription = it },
                        features = features, onRemoveFeature = { features.remove(it) }, onAddFeature = { features.add(it) }
                    )

                    1 -> StepMedia(
                        tokens = tokens,
                        images = uploadedImages,
                        onBrowse = { imagePicker.launch("image/*") },
                        onRemoveImage = { uploadedImages.remove(it) }
                    )

                    2 -> StepVariants(
                        tokens = tokens,
                        sizes = sizes,
                        colors = colors,
                        variants = generatedVariants,
                        onAddSize = { sizes.add(it) },
                        onRemoveSize = { sizes.remove(it) },
                        onAddColor = { colors.add(it) },
                        onRemoveColor = { colors.remove(it) }
                    )

                    3 -> StepPricing(
                        tokens = tokens,
                        currency = currency, currencyExpanded = currencyExpanded,
                        onCurrencyExpanded = { currencyExpanded = it }, onCurrencySelected = { currency = it },
                        taxCategory = taxCategory, taxExpanded = taxExpanded,
                        onTaxExpanded = { taxExpanded = it }, onTaxSelected = { taxCategory = it },
                        sellingPrice = sellingPrice, onSellingPriceChange = { sellingPrice = it },
                        discountPrice = discountPrice, onDiscountPriceChange = { discountPrice = it },
                        pricingTiers = pricingTiers,
                        onDeleteTier = { pricingTiers.remove(it) },
                        onAddTier = {
                            pricingTiers.add(
                                PricingTierRow(
                                    tierLabel = "TIER ${pricingTiers.size + 1}",
                                    minQty = "",
                                    maxQty = "",
                                    unitPrice = "",
                                    discountPercent = "0% off"
                                )
                            )
                        }
                    )

                    4 -> StepInventory(
                        tokens = tokens,
                        trackInventory = trackInventory, onTrackInventoryChange = { trackInventory = it },
                        openingStock = openingStock, onOpeningStockChange = { openingStock = it },
                        openingStockRate = openingStockRate, onOpeningStockRateChange = { openingStockRate = it },
                        reorderLevel = reorderLevel, onReorderLevelChange = { reorderLevel = it },
                        safetyStock = safetyStock, onSafetyStockChange = { safetyStock = it },
                        emailAlerts = emailAlerts, onEmailAlertsChange = { emailAlerts = it },
                        dashboardAlerts = dashboardAlerts, onDashboardAlertsChange = { dashboardAlerts = it },
                        warehouses = warehouses
                    )

                    5 -> StepChannels(
                        tokens = tokens,
                        onlineStoreEnabled = onlineStoreEnabled, onOnlineStoreChange = { onlineStoreEnabled = it },
                        retailPosEnabled = retailPosEnabled, onRetailPosChange = { retailPosEnabled = it },
                        marketplaceEnabled = marketplaceEnabled, onMarketplaceChange = { marketplaceEnabled = it },
                        wholesaleEnabled = wholesaleEnabled, onWholesaleChange = { wholesaleEnabled = it },
                        onlineCustomPrice = onlineCustomPrice, onOnlinePriceChange = { onlineCustomPrice = it },
                        retailCustomPrice = retailCustomPrice, onRetailPriceChange = { retailCustomPrice = it }
                    )

                    6 -> StepSEO(
                        tokens = tokens,
                        seoTitle = seoTitle, onSeoTitleChange = { seoTitle = it },
                        metaDescription = metaDescription, onMetaDescriptionChange = { metaDescription = it },
                        urlSlug = urlSlug, onUrlSlugChange = { urlSlug = it },
                        searchTags = searchTags, onRemoveTag = { searchTags.remove(it) }, onAddTag = { searchTags.add(it) }
                    )

                    7 -> StepPreview(
                        tokens = tokens,
                        productName = productName,
                        category = category,
                        brand = brand,
                        sellingPrice = sellingPrice,
                        openingStock = openingStock,
                        sku = sku
                    )

                    8 -> StepPublish(
                        tokens = tokens,
                        publishOption = publishOption, onPublishOptionChange = { publishOption = it },
                        visibilityOption = visibilityOption, onVisibilityOptionChange = { visibilityOption = it },
                        productName = productName,
                        category = category
                    )
                }
            }
        }

        // Bottom Navigation Bar
        if (currentStep < 8) {
            StepNavigationFab(
                showBack = currentStep > 0,
                onBack = { if (currentStep > 0) currentStep-- },
                backLabel = if (currentStep == 0) "Cancel" else "Previous",
                trailingAction = TrailingFabAction.Next(
                    label = "Next: ${steps[currentStep + 1]}",
                    onClick = { if (currentStep < 8) currentStep++ }
                ),
                backWidthFraction = 0.35f,
                trailingWidthFraction = 0.45f,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(whiteBg)
                    .border(BorderStroke(1.dp, grey_border))
                    .padding(tokens.screenPadding),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackFabButton(
                    onClick = { currentStep-- },
                    label = "Back",
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { onClose() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg, contentColor = TextPrimary),
                    border = BorderStroke(1.dp, grey_border),
                    modifier = Modifier.weight(1.2f).height(tokens.buttonHeight)
                ) {
                    Text("Save Draft", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { onClose() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.weight(1.5f).height(tokens.buttonHeight)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_rocket), contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Publish Product", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = whiteBg)
                }
            }
        }
    }
}

// ==============================================================================
// 3. STEP COMPONENTS
// ==============================================================================

/** STEP 1: Basic Information */
@Composable
private fun StepBasicInfo(
    tokens: AppDesignTokens,
    productName: String, onProductNameChange: (String) -> Unit,
    sku: String, onSkuChange: (String) -> Unit,
    isAutoSku: Boolean, onAutoSkuChange: (Boolean) -> Unit,
    productType: String, productTypeExpanded: Boolean, onProductTypeExpanded: (Boolean) -> Unit, onProductTypeSelected: (String) -> Unit,
    category: String, categoryExpanded: Boolean, onCategoryExpanded: (Boolean) -> Unit, onCategorySelected: (String) -> Unit,
    subCategory: String, subCategoryExpanded: Boolean, onSubCategoryExpanded: (Boolean) -> Unit, onSubCategorySelected: (String) -> Unit,
    brand: String, onBrandChange: (String) -> Unit,
    manufacturer: String, onManufacturerChange: (String) -> Unit,
    productStatus: String, onProductStatusChange: (String) -> Unit,
    fabric: String, onFabricChange: (String) -> Unit,
    pattern: String, onPatternChange: (String) -> Unit,
    sleeveType: String, onSleeveTypeChange: (String) -> Unit,
    material: String, onMaterialChange: (String) -> Unit,
    fitType: String, fitTypeExpanded: Boolean, onFitTypeExpanded: (Boolean) -> Unit, onFitTypeSelected: (String) -> Unit,
    collarType: String, collarTypeExpanded: Boolean, onCollarTypeExpanded: (Boolean) -> Unit, onCollarTypeSelected: (String) -> Unit,
    shortDescription: String, onShortDescriptionChange: (String) -> Unit,
    longDescription: String, onLongDescriptionChange: (String) -> Unit,
    features: List<String>, onRemoveFeature: (String) -> Unit, onAddFeature: (String) -> Unit
) {
    var identityExpanded by remember { mutableStateOf(true) }
    var attributesExpanded by remember { mutableStateOf(true) }
    var descriptionExpanded by remember { mutableStateOf(true) }
    var featureInputText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        // Section: Product Identity
        item {
            FormAccordionCard(
                title = "Product Identity",
                isRoundedCorner = true,
                iconPainter = R.drawable.ic_fingerprint,
                expanded = identityExpanded,
                onHeaderClick = { identityExpanded = !identityExpanded }
            ) {
                FormLabel("Product Name")
                FormTextField(
                    value = productName,
                    onValueChange = onProductNameChange,
                    placeholder = "e.g. Premium Cotton Formal Shirt"
                )

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormLabel("SKU")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Auto-generate", fontSize = tokens.caption, color = headerGrey)
                        Spacer(Modifier.width(6.dp))
                        MiniSwitch(checked = isAutoSku, onCheckedChange = onAutoSkuChange)
                    }
                }
                FormTextField(
                    value = sku,
                    onValueChange = onSkuChange,
                    placeholder = "e.g. CTS-001",
                    enabled = !isAutoSku
                )

                Spacer(Modifier.height(14.dp))
                FormLabel("Product Type")
                FormDropdown(
                    value = productType,
                    expanded = productTypeExpanded,
                    onExpandChange = onProductTypeExpanded,
                    options = listOf("Physical Product", "Digital Service", "Custom Tailoring"),
                    onOptionSelected = onProductTypeSelected
                )

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Category")
                        FormDropdown(
                            value = category,
                            expanded = categoryExpanded,
                            onExpandChange = onCategoryExpanded,
                            options = listOf("Shirts", "Suits", "Trousers"),
                            onOptionSelected = onCategorySelected
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Sub Category")
                        FormDropdown(
                            value = subCategory,
                            expanded = subCategoryExpanded,
                            onExpandChange = onSubCategoryExpanded,
                            options = listOf("Formal Shirts", "Casual Shirts"),
                            onOptionSelected = onSubCategorySelected
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Brand")
                        FormTextField(
                            value = brand,
                            onValueChange = onBrandChange,
                            placeholder = "e.g. Cuso Tailor"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Manufacturer")
                        FormTextField(
                            value = manufacturer,
                            onValueChange = onManufacturerChange,
                            placeholder = "e.g. In-house"
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                FormLabel("Product Status")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("Draft", "Active", "Archived").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onProductStatusChange(option) }
                        ) {
                            AppRadioButton(
                                selected = productStatus == option,
                                onClick = { onProductStatusChange(option) }
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(option, fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Section: Product Attributes
        item {
            FormAccordionCard(
                title = "Product Attributes",
                isRoundedCorner = true,
                icon = Icons.Default.Tune,
                expanded = attributesExpanded,
                onHeaderClick = { attributesExpanded = !attributesExpanded }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Fabric")
                        FormTextField(
                            value = fabric,
                            onValueChange = onFabricChange,
                            placeholder = "e.g. Cotton"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Pattern")
                        FormTextField(
                            value = pattern,
                            onValueChange = onPatternChange,
                            placeholder = "e.g. Plain"
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Sleeve Type")
                        FormTextField(
                            value = sleeveType,
                            onValueChange = onSleeveTypeChange,
                            placeholder = "e.g. Full"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Material")
                        FormTextField(
                            value = material,
                            onValueChange = onMaterialChange,
                            placeholder = "e.g. Cotton Blend"
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Fit Type")
                        FormDropdown(
                            value = fitType,
                            expanded = fitTypeExpanded,
                            onExpandChange = onFitTypeExpanded,
                            options = listOf("Slim Fit", "Regular Fit", "Relaxed Fit"),
                            onOptionSelected = onFitTypeSelected
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Collar Type")
                        FormDropdown(
                            value = collarType,
                            expanded = collarTypeExpanded,
                            onExpandChange = onCollarTypeExpanded,
                            options = listOf("Spread Collar", "Mandarin", "Button-down"),
                            onOptionSelected = onCollarTypeSelected
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = background_light_purple,
                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                        )
                        .padding(
                            horizontal = tokens.screenPadding * 1.1f,
                            vertical = tokens.screenPadding * 0.9f
                        ),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bulb),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(tokens.iconSize * 1.1f)
                    )

                    Spacer(Modifier.width(tokens.extraPadding * 1.2f))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Quick Tip",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )

                        Text(
                            text = "Add detailed features to help customers find your product through search filters.",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            lineHeight = (tokens.bodyMedium.value * 1.35f).sp
                        )
                    }
                }
            }
        }

        // Section: Product Description
        item {
            FormAccordionCard(
                title = "Product Description",
                isRoundedCorner = true,
                icon = Icons.Default.Description,
                expanded = descriptionExpanded,
                onHeaderClick = { descriptionExpanded = !descriptionExpanded }
            ) {
                FormLabel("Short Description")
                FormTextField(
                    value = shortDescription,
                    onValueChange = onShortDescriptionChange,
                    placeholder = "A brief overview of the product..."
                )

                Spacer(Modifier.height(14.dp))
                FormLabel("Long Description")

                var richTextValue by remember(longDescription) {
                    mutableStateOf(
                        TextFieldValue(
                            text = longDescription,
                            selection = TextRange(longDescription.length)
                        )
                    )
                }

                RichTextEditorArea(
                    value = richTextValue,
                    onValueChange = { newValue ->
                        richTextValue = newValue
                        onLongDescriptionChange(newValue.text)
                    },
                    placeholder = "Detailed product details, styling tips, etc."
                )

                Spacer(Modifier.height(14.dp))
                FormLabel("Feature List")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormTextField(
                        value = featureInputText,
                        onValueChange = { featureInputText = it },
                        placeholder = "Add feature and tap '+'...",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (featureInputText.isNotBlank()) {
                                onAddFeature(featureInputText.trim())
                                featureInputText = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Primary)
                    }
                }

                if (features.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        features.forEach { feature ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = primary_light,
                                border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = feature,
                                        fontSize = tokens.caption,
                                        color = Primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Feature",
                                        tint = Primary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { onRemoveFeature(feature) }
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

/** STEP 2: Media Information */
@Composable
private fun StepMedia(
    tokens: AppDesignTokens,
    images: List<Uri>,
    onBrowse: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    var imagesExpanded by remember { mutableStateOf(true) }
    var videoExpanded by remember { mutableStateOf(true) }
    var view360Expanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        item {
            FormAccordionCard(
                title = "Product Images",
                isRoundedCorner = true,
                icon = Icons.Default.Image,
                expanded = imagesExpanded,
                onHeaderClick = { imagesExpanded = !imagesExpanded }
            ) {
                Text(
                    "Add high-quality images to showcase your product from every angle.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )
                Spacer(Modifier.height(10.dp))
                ImageUploadSection(
                    isImage = false,
                    selectedImages = images,
                    onBrowseClick = onBrowse,
                    onCameraClick = null,
                    onRemoveImage = { removedUri ->
                        onRemoveImage(removedUri)
                    },
                    browseText = "Browse Files",
                    documentUploadText = "Drag & drop product images\nRecommended: JPG, PNG.",
                    imagePreviewSize = 86.dp,
                    previewHeaderTitle = "UPLOADED ANGLES"
                )
            }
        }

        item {
            FormAccordionCard(
                title = "Product Video",
                icon = Icons.Default.Videocam,
                isRoundedCorner = true,
                expanded = videoExpanded,
                onHeaderClick = { videoExpanded = !videoExpanded }
            ) {
                Text(
                    "Short clips (up to 30s) increase conversion by 30%.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_blue, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(primary_light, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Upload video", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("MP4, MOV. Max size: 50MB", fontSize = tokens.caption, color = headerGrey)
                        }
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = primary_light, contentColor = Primary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Select Video", fontSize = tokens.caption, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        item {
            FormAccordionCard(
                title = "360° Product View",
                isRoundedCorner = true,
                icon = Icons.AutoMirrored.Filled.RotateRight,
                expanded = view360Expanded,
                onHeaderClick = { view360Expanded = !view360Expanded }
            ) {
                Text(
                    "Upload a sequence of rotation images.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_blue, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).background(primary_light, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Sequence images", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("Upload 24 or 36 images", fontSize = tokens.caption, color = headerGrey)
                        }
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = primary_light, contentColor = Primary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Add Frames", fontSize = tokens.caption, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

/** STEP 3: Variant Information */
@Composable
private fun StepVariants(
    tokens: AppDesignTokens,
    sizes: List<String>,
    colors: List<String>,
    variants: List<ItemVariantRow>,
    onAddSize: (String) -> Unit,
    onRemoveSize: (String) -> Unit,
    onAddColor: (String) -> Unit,
    onRemoveColor: (String) -> Unit
) {
    var attrExpanded by remember { mutableStateOf(true) }
    var listExpanded by remember { mutableStateOf(true) }

    // Toggle state to switch between showing top 5 items and all items
    var isViewAllExpanded by remember { mutableStateOf(false) }

    val activeAttributeTypes = remember { mutableStateListOf<String>() }
    val attributeValuesMap = remember { mutableStateMapOf<String, List<String>>() }

    // Dynamic list holding variants generated strictly from user selections
    val generatedVariantList = remember { mutableStateListOf<ItemVariantRow>() }

    // Helper function to safely parse hex colors or color names
    fun parseColorSafe(colorString: String): Color {
        val cleanColor = colorString.trim()
        return try {
            if (cleanColor.startsWith("#")) {
                Color(cleanColor.toColorInt())
            } else if (cleanColor.length == 6 || cleanColor.length == 8) {
                Color("#$cleanColor".toColorInt())
            } else {
                when (cleanColor.lowercase()) {
                    "white" -> Color.White
                    "black" -> Color.Black
                    "blue" -> Color(0xFF2563EB)
                    "red" -> Color(0xFFDC2626)
                    "green" -> Color(0xFF16A34A)
                    "grey", "gray" -> Color(0xFF6B7280)
                    else -> Color.Transparent
                }
            }
        } catch (_: Exception) {
            Color.Transparent
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        // ── 1. Variant Attributes Card ──
        item {
            FormAccordionCard(
                title = "Variant Attributes",
                isRoundedCorner = true,
                icon = Icons.Default.Style,
                expanded = attrExpanded,
                onHeaderClick = { attrExpanded = !attrExpanded }
            ) {
                Text(
                    text = "Select attribute types, choose values, then tap 'Generate Variants'.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                Spacer(Modifier.height(14.dp))
                FormLabel("Attribute Type")

                // Checkbox selector for Size / Color
                AttributeTypeSelector(
                    selectedTypes = activeAttributeTypes.toSet(),
                    onTypeToggled = { type, isChecked ->
                        if (isChecked) {
                            if (!activeAttributeTypes.contains(type)) {
                                activeAttributeTypes.add(type)
                            }
                        } else {
                            activeAttributeTypes.remove(type)
                            attributeValuesMap.remove(type)
                        }
                    },
                    tokens = tokens
                )

                // Dynamic value pickers for each selected attribute type
                activeAttributeTypes.forEach { type ->
                    Spacer(Modifier.height(tokens.extraPadding))
                    Column {
                        Text(
                            text = "$type Values",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(6.dp))
                        AttributeValueSelector(
                            attributeType = type,
                            values = attributeValuesMap[type] ?: emptyList(),
                            onValuesChange = { newValues ->
                                attributeValuesMap[type] = newValues
                            },
                            tokens = tokens
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                AppButton(
                    text = "✨ Generate Variants",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val validMap = attributeValuesMap.filter { it.key in activeAttributeTypes && it.value.isNotEmpty() }
                        val combinations = generateAttributeCombinations(validMap)

                        generatedVariantList.clear()
                        isViewAllExpanded = false // Reset back to showing top 5 on new generation

                        combinations.forEach { combo ->
                            val sizeVal = if (validMap.containsKey("Size")) {
                                val sizeIndex = validMap.keys.indexOf("Size")
                                combo.getOrNull(sizeIndex) ?: ""
                            } else ""

                            val colorVal = if (validMap.containsKey("Color")) {
                                val colorIndex = validMap.keys.indexOf("Color")
                                combo.getOrNull(colorIndex) ?: ""
                            } else ""

                            val titleParts = listOfNotNull(
                                sizeVal.takeIf { it.isNotBlank() },
                                colorVal.takeIf { it.isNotBlank() }
                            )
                            val label = titleParts.joinToString(" ")
                            val skuSuffix = titleParts.joinToString("-") { it.filter { ch -> ch.isLetterOrDigit() }.take(3).uppercase() }

                            generatedVariantList.add(
                                ItemVariantRow(
                                    title = label,
                                    size = sizeVal,
                                    color = colorVal,
                                    price = "45",
                                    sku = if (skuSuffix.isNotBlank()) "SH-$skuSuffix" else "SH-GEN",
                                    stock = "20",
                                    isActive = true
                                )
                            )
                        }

                        listExpanded = true
                    }
                )
            }
        }

        // ── 2. Generated Variant List Card ──
        item {
            FormAccordionCard(
                title = "Variant List (${generatedVariantList.size})",
                isRoundedCorner = true,
                icon = Icons.AutoMirrored.Filled.ListAlt,
                expanded = listExpanded,
                onHeaderClick = { listExpanded = !listExpanded }
            ) {
                if (generatedVariantList.isEmpty()) {
                    Text(
                        text = "No variants generated yet. Select attributes, pick values above, and tap 'Generate Variants'.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                } else {
                    // Action Toolbar: Edit Price, Edit Stock, Active Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, grey_border),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = whiteBg,
                                    contentColor = TextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = headerGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Edit Price",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            OutlinedButton(
                                onClick = { },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, grey_border),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = whiteBg,
                                    contentColor = TextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_calendar),
                                    contentDescription = null,
                                    tint = headerGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Edit Stock",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            MiniSwitch(
                                checked = true,
                                onCheckedChange = { }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Determine items to display: top 5 items or all items
                    val displayedVariants = if (isViewAllExpanded) {
                        generatedVariantList
                    } else {
                        generatedVariantList.take(5)
                    }

                    // Variant Items List
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        displayedVariants.forEachIndexed { index, variant ->
                            val parsedColor = parseColorSafe(variant.color)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                // Row 1: [Color Preview Box] + [Color Hex Code], [● Active Badge], [More Options]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Color Preview Box replacing the top text
                                        if (variant.color.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(parsedColor)
                                                    .border(1.dp, grey_border, RoundedCornerShape(4.dp))
                                            )
                                        }

                                        // Color Hex Code as primary header text
                                        Text(
                                            text = variant.color.ifBlank { variant.title },
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }

                                    // Active Badge & More Options Menu
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = greenBg
                                        ) {
                                            Text(
                                                text = if (variant.isActive) "● Active" else "● Inactive",
                                                fontSize = tokens.label,
                                                fontWeight = FontWeight.Medium,
                                                color = darkGreenBg,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = headerGrey,
                                            modifier = Modifier
                                                .size(tokens.iconSize * 1.1f)
                                                .clickable { }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                // Row 2: Size on the left & Price on the right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (variant.size.isNotBlank()) "Size: ${variant.size}" else "Size: —",
                                        fontSize = tokens.bodySmall,
                                        color = headerGrey
                                    )

                                    Text(
                                        text = "₹ ${variant.price}",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = title_border, thickness = 0.8.dp)
                                Spacer(Modifier.height(6.dp))

                                // Row 3: SKU & Stock Count
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "SKU: ",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                        Text(
                                            text = variant.sku,
                                            fontSize = tokens.caption,
                                            color = headerGrey
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Stock: ",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                        Text(
                                            text = variant.stock,
                                            fontSize = tokens.caption,
                                            color = headerGrey
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // View All button appears only when there are more than 5 items
                    if (generatedVariantList.size > 5) {
                        Spacer(Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isViewAllExpanded = !isViewAllExpanded
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isViewAllExpanded) {
                                    "Show Less"
                                } else {
                                    "View All ${generatedVariantList.size} Variants"
                                },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }
                    }
                }
            }
        }
    }
}
/** STEP 4: Pricing Information */
@Composable
private fun StepPricing(
    tokens: AppDesignTokens,
    currency: String, currencyExpanded: Boolean, onCurrencyExpanded: (Boolean) -> Unit, onCurrencySelected: (String) -> Unit,
    taxCategory: String, taxExpanded: Boolean, onTaxExpanded: (Boolean) -> Unit, onTaxSelected: (String) -> Unit,
    sellingPrice: String, onSellingPriceChange: (String) -> Unit,
    discountPrice: String, onDiscountPriceChange: (String) -> Unit,
    pricingTiers: List<PricingTierRow>,
    onDeleteTier: (PricingTierRow) -> Unit,
    onAddTier: () -> Unit
) {
    var pricingExpanded by remember { mutableStateOf(true) }
    var tiersExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        item {
            FormAccordionCard(
                title = "Sales Pricing",
                isRoundedCorner = true,
                icon = Icons.Default.AttachMoney,
                expanded = pricingExpanded,
                onHeaderClick = { pricingExpanded = !pricingExpanded }
            ) {
                Text("Set your base selling price and applicable taxes.", fontSize = tokens.caption, color = headerGrey)
                Spacer(Modifier.height(10.dp))

                FormLabel("Currency")
                FormDropdown(
                    value = currency,
                    expanded = currencyExpanded,
                    onExpandChange = onCurrencyExpanded,
                    options = listOf("INR - Indian Rupee", "USD - US Dollar"),
                    onOptionSelected = onCurrencySelected
                )

                Spacer(Modifier.height(14.dp))
                FormLabel("Tax Category")
                FormDropdown(
                    value = taxCategory,
                    expanded = taxExpanded,
                    onExpandChange = onTaxExpanded,
                    options = listOf("GST 0%", "GST 5%", "GST 12%", "GST 18%"),
                    onOptionSelected = onTaxSelected
                )

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Selling Price (₹)")
                        FormTextField(
                            value = sellingPrice,
                            onValueChange = onSellingPriceChange,
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Discount Price (₹)")
                        FormTextField(
                            value = discountPrice,
                            onValueChange = onDiscountPriceChange,
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }
        }

        item {
            FormAccordionCard(
                title = "Pricing Tiers",
                isRoundedCorner = true,
                icon = Icons.Default.Layers,
                expanded = tiersExpanded,
                onHeaderClick = { tiersExpanded = !tiersExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "+ Add Tier",
                        fontSize = tokens.caption,
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onAddTier() }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Define bulk discounts and wholesale price levels.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (pricingTiers.isEmpty()) {
                    Text("No pricing tiers added. Tap '+ Add Tier' to configure volume discounts.", fontSize = tokens.caption, color = headerGrey)
                } else {
                    pricingTiers.forEach { tier ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tier.tierLabel, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = greenBg) {
                                        Text(tier.discountPercent, fontSize = tokens.label, color = darkGreenBg, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = redText,
                                        modifier = Modifier.size(16.dp).clickable { onDeleteTier(tier) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                FormTextField(value = tier.minQty, onValueChange = {}, placeholder = "Min", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                                Text("—", color = headerGrey)
                                FormTextField(value = tier.maxQty, onValueChange = {}, placeholder = "Max", modifier = Modifier.weight(1f))
                                FormTextField(value = tier.unitPrice, onValueChange = {}, placeholder = "Price", modifier = Modifier.weight(1.5f), keyboardType = KeyboardType.Number)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** STEP 5: Inventory Information */
@Composable
private fun StepInventory(
    tokens: AppDesignTokens,
    trackInventory: Boolean,
    onTrackInventoryChange: (Boolean) -> Unit,
    openingStock: String,
    onOpeningStockChange: (String) -> Unit,
    openingStockRate: String,
    onOpeningStockRateChange: (String) -> Unit,
    reorderLevel: String,
    onReorderLevelChange: (String) -> Unit,
    safetyStock: String,
    onSafetyStockChange: (String) -> Unit,
    emailAlerts: Boolean,
    onEmailAlertsChange: (Boolean) -> Unit,
    dashboardAlerts: Boolean,
    onDashboardAlertsChange: (Boolean) -> Unit,
    warehouses: List<WarehouseStockRow>
) {
    var stockDetailsExpanded by remember { mutableStateOf(true) }
    var alertsExpanded by remember { mutableStateOf(true) }
    var warehouseExpanded by remember { mutableStateOf(true) }

    // Dynamic calculation of Estimated Value = Opening Stock × Stock Rate
    val calculatedEstimatedValue = remember(openingStock, openingStockRate) {
        val qty = openingStock.toDoubleOrNull() ?: 0.0
        val rate = openingStockRate.toDoubleOrNull() ?: 0.0
        val total = qty * rate
        String.format(Locale.US, "$%,.2f", total)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        // ── 1. Track Inventory Master Switch Card ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, grey_border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Track Inventory",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Enable this to automatically manage stock counts and receive notifications when stock is low.",
                                fontSize = tokens.caption,
                                color = headerGrey,
                                lineHeight = (tokens.caption.value * 1.35f).sp
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    MiniSwitch(
                        checked = trackInventory,
                        onCheckedChange = onTrackInventoryChange
                    )
                }
            }
        }

        // ── 2. Stock Details Card ──
        item {
            FormAccordionCard(
                title = "Stock Details",
                isRoundedCorner = true,
                icon = Icons.Default.Description,
                expanded = stockDetailsExpanded,
                onHeaderClick = { stockDetailsExpanded = !stockDetailsExpanded }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Opening Stock")
                        FormTextField(
                            value = openingStock,
                            onValueChange = onOpeningStockChange,
                            placeholder = "120",
                            keyboardType = KeyboardType.Number,
                            enabled = trackInventory
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Opening Stock Rate ($)")
                        FormTextField(
                            value = openingStockRate,
                            onValueChange = onOpeningStockRateChange,
                            placeholder = "22",
                            keyboardType = KeyboardType.Number,
                            enabled = trackInventory
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Reorder Level")
                        FormTextField(
                            value = reorderLevel,
                            onValueChange = onReorderLevelChange,
                            placeholder = "20",
                            keyboardType = KeyboardType.Number,
                            enabled = trackInventory
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Safety Stock")
                        FormTextField(
                            value = safetyStock,
                            onValueChange = onSafetyStockChange,
                            placeholder = "10",
                            keyboardType = KeyboardType.Number,
                            enabled = trackInventory
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Highlight Banner: Estimated Value
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primary_light, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Estimated Value",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                            Text(
                                text = calculatedEstimatedValue,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Text(
                        text = "Opening Stock\n× Stock Rate",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // ── 3. Low Stock Alerts Card ──
        item {
            FormAccordionCard(
                title = "Low Stock Alerts",
                isRoundedCorner = true,
                icon = Icons.Default.Notifications,
                expanded = alertsExpanded,
                onHeaderClick = { alertsExpanded = !alertsExpanded }
            ) {
                // Email Alerts Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg, RoundedCornerShape(8.dp))
                        .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MailOutline,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Email Alerts",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Notify procurement team",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }

                    MiniSwitch(
                        checked = emailAlerts,
                        onCheckedChange = onEmailAlertsChange
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Dashboard Alerts Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg, RoundedCornerShape(8.dp))
                        .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Dashboard Alerts",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Show in notifications hub",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }

                    MiniSwitch(
                        checked = dashboardAlerts,
                        onCheckedChange = onDashboardAlertsChange
                    )
                }

                Spacer(Modifier.height(14.dp))
                FormLabel("Alert Recipients")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primary_light
                    ) {
                        Text(
                            text = "John Doe (Admin)",
                            fontSize = tokens.caption,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = primary_light
                    ) {
                        Text(
                            text = "Purchasing Dep.",
                            fontSize = tokens.caption,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ── 4. Warehouse Distribution Card ──
        item {
            FormAccordionCard(
                title = "Warehouse Distribution",
                isRoundedCorner = true,
                icon = Icons.Default.Apartment,
                expanded = warehouseExpanded,
                onHeaderClick = { warehouseExpanded = !warehouseExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "+ Add Warehouse",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(Modifier.height(8.dp))

                val sampleWarehouses = warehouses.ifEmpty {
                    listOf(
                        WarehouseStockRow(name = "Main Warehouse", icon = Icons.Default.Place, stockQty = "80"),
                        WarehouseStockRow(name = "Retail Store", icon = Icons.Default.ShoppingBag, stockQty = "40")
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sampleWarehouses.forEach { wh ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(badgeGrey, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(primary_light),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = wh.icon,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = wh.name,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = wh.stockQty,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = headerGrey,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** STEP 6: Channels Information */
@Composable
private fun StepChannels(
    tokens: AppDesignTokens,
    onlineStoreEnabled: Boolean,
    onOnlineStoreChange: (Boolean) -> Unit,
    retailPosEnabled: Boolean,
    onRetailPosChange: (Boolean) -> Unit,
    marketplaceEnabled: Boolean,
    onMarketplaceChange: (Boolean) -> Unit,
    wholesaleEnabled: Boolean,
    onWholesaleChange: (Boolean) -> Unit,
    onlineCustomPrice: String,
    onOnlinePriceChange: (String) -> Unit,
    retailCustomPrice: String,
    onRetailPriceChange: (String) -> Unit
) {
    var channelsExpanded by remember { mutableStateOf(true) }
    var onlineConfigExpanded by remember { mutableStateOf(true) }
    var retailConfigExpanded by remember { mutableStateOf(true) }

    var onlineVisibilityExpanded by remember { mutableStateOf(false) }
    var onlineVisibility by remember { mutableStateOf("Select an option") }

    var retailVisibilityExpanded by remember { mutableStateOf(false) }
    var retailVisibility by remember { mutableStateOf("Select an option") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        item {
            FormAccordionCard(
                title = "Sales Channels",
                isRoundedCorner = true,
                icon = Icons.Default.Share,
                expanded = channelsExpanded,
                onHeaderClick = { channelsExpanded = !channelsExpanded }
            ) {
                Text("Select where you want your product to be available for purchase.", fontSize = tokens.caption, color = headerGrey)
                Spacer(Modifier.height(10.dp))

                ChannelToggleRow(tokens = tokens, title = "Online Store", subtitle = "Your main e-commerce website", icon = Icons.Default.Language, checked = onlineStoreEnabled, onCheckedChange = onOnlineStoreChange)
                Spacer(Modifier.height(10.dp))
                ChannelToggleRow(tokens = tokens, title = "Retail POS", subtitle = "Physical store locations", icon = Icons.Default.Store, checked = retailPosEnabled, onCheckedChange = onRetailPosChange)
                Spacer(Modifier.height(10.dp))
                ChannelToggleRow(tokens = tokens, title = "Marketplace", subtitle = "Amazon, eBay, and Etsy", icon = Icons.Default.ShoppingBag, checked = marketplaceEnabled, onCheckedChange = onMarketplaceChange)
                Spacer(Modifier.height(10.dp))
                ChannelToggleRow(tokens = tokens, title = "Wholesale", subtitle = "B2B and bulk orders", icon = Icons.Default.Groups, checked = wholesaleEnabled, onCheckedChange = onWholesaleChange)
            }
        }

        if (onlineStoreEnabled) {
            item {
                FormAccordionCard(
                    title = "Online Store Configuration",
                    isRoundedCorner = true,
                    icon = Icons.Default.Language,
                    expanded = onlineConfigExpanded,
                    onHeaderClick = { onlineConfigExpanded = !onlineConfigExpanded }
                ) {
                    FormLabel("Custom Price (INR)")
                    PriceInputField(value = onlineCustomPrice, onValueChange = onOnlinePriceChange, tokens = tokens)

                    Spacer(Modifier.height(14.dp))
                    FormLabel("Visibility Status")
                    FormDropdown(
                        value = onlineVisibility,
                        expanded = onlineVisibilityExpanded,
                        onExpandChange = { onlineVisibilityExpanded = it },
                        options = listOf("Visible to everyone", "Hidden from catalog", "Members only"),
                        onOptionSelected = { onlineVisibility = it }
                    )
                }
            }
        }

        if (retailPosEnabled) {
            item {
                FormAccordionCard(
                    title = "Retail POS Configuration",
                    isRoundedCorner = true,
                    icon = Icons.Default.Store,
                    expanded = retailConfigExpanded,
                    onHeaderClick = { retailConfigExpanded = !retailConfigExpanded }
                ) {
                    FormLabel("Custom Price (INR)")
                    PriceInputField(value = retailCustomPrice, onValueChange = onRetailPriceChange, tokens = tokens)

                    Spacer(Modifier.height(14.dp))
                    FormLabel("Store Visibility")
                    FormDropdown(
                        value = retailVisibility,
                        expanded = retailVisibilityExpanded,
                        onExpandChange = { retailVisibilityExpanded = it },
                        options = listOf("All physical locations", "Main Branch only", "Specific warehouses"),
                        onOptionSelected = { retailVisibility = it }
                    )
                }
            }
        }
    }
}

/** STEP 7: SEO Information */
@Composable
private fun StepSEO(
    tokens: AppDesignTokens,
    seoTitle: String, onSeoTitleChange: (String) -> Unit,
    metaDescription: String, onMetaDescriptionChange: (String) -> Unit,
    urlSlug: String, onUrlSlugChange: (String) -> Unit,
    searchTags: List<String>, onRemoveTag: (String) -> Unit, onAddTag: (String) -> Unit
) {
    var seoExpanded by remember { mutableStateOf(true) }
    var tagInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        item {
            FormAccordionCard(
                title = "SEO Details",
                isRoundedCorner = true,
                icon = Icons.Default.Search,
                expanded = seoExpanded,
                onHeaderClick = { seoExpanded = !seoExpanded }
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FormLabel("SEO Title")
                    Text("${seoTitle.length} / 60", fontSize = tokens.caption, color = headerGrey)
                }
                FormTextField(value = seoTitle, onValueChange = onSeoTitleChange, placeholder = "Enter SEO title...")

                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FormLabel("Meta Description")
                    Text("${metaDescription.length} / 160", fontSize = tokens.caption, color = headerGrey)
                }
                FormTextArea(value = metaDescription, onValueChange = onMetaDescriptionChange, placeholder = "Enter meta description...")

                Spacer(Modifier.height(14.dp))
                FormLabel("URL Slug")
                FormTextField(value = urlSlug, onValueChange = onUrlSlugChange, placeholder = "e.g. premium-cotton-formal-shirt")

                Spacer(Modifier.height(14.dp))
                FormLabel("Search Tags")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = "Add search tag...",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (tagInput.isNotBlank()) {
                                onAddTag(tagInput.trim())
                                tagInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Tag", tint = Primary)
                    }
                }

                if (searchTags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        searchTags.forEach { tag ->
                            Surface(shape = RoundedCornerShape(4.dp), color = primary_light) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(tag, fontSize = tokens.caption, color = Primary)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp).clickable { onRemoveTag(tag) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** STEP 8: Preview */
@Composable
private fun StepPreview(
    tokens: AppDesignTokens,
    productName: String,
    category: String,
    brand: String,
    sellingPrice: String,
    openingStock: String,
    sku: String
) {
    var generalInfoExpanded by remember { mutableStateOf(true) }
    var descriptionExpanded by remember { mutableStateOf(false) }
    var variantSummaryExpanded by remember { mutableStateOf(false) }
    var pricingExpanded by remember { mutableStateOf(true) }
    var inventoryExpanded by remember { mutableStateOf(true) }

    var selectedPreviewColor by remember { mutableStateOf(Color(0xFF2F27CE)) }
    var selectedPreviewSize by remember { mutableStateOf("M") }

    val sampleColorSwatches = listOf(
        Color(0xFF2F27CE),
        Color(0xFF1E293B),
        Color(0xFF000000),
        Color(0xFF94A3B8)
    )
    val sizeOptions = listOf("XS", "S", "M", "L", "XL", "XXL")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        // ── 1. Live Preview Canvas & Product Header ──
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = BorderStroke(1.dp, grey_border)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    // Preview Photo Canvas with Top Right Live Preview Tag
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(light_blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = whiteBg,
                            border = BorderStroke(1.dp, grey_border),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "LIVE PREVIEW",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(whiteBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = mutedText,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Product Photo Canvas",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "New Product",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = productName.ifBlank { "Premium Cotton Formal Shirt" },
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "A versatile essential for the modern wardrobe, crafted from 100% long-staple cotton for breathability and a crisp finish.",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        lineHeight = (tokens.caption.value * 1.35f).sp
                    )

                    Spacer(Modifier.height(14.dp))

                    // Base Price and Total Stock Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(badgeGrey, RoundedCornerShape(8.dp))
                                .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "BASE PRICE",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.SemiBold,
                                color = headerGrey
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (sellingPrice.isNotBlank()) "₹$sellingPrice.00" else "₹45.00",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(badgeGrey, RoundedCornerShape(8.dp))
                                .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "TOTAL STOCK",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.SemiBold,
                                color = headerGrey
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (openingStock.isNotBlank()) "$openingStock Units" else "120 Units",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Color Verification",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))

                    // Color verification circles
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sampleColorSwatches.forEach { color ->
                            val isSelected = selectedPreviewColor == color
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Primary else grey_border,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedPreviewColor = color }
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Size Configuration",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))

                    // Size selector chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sizeOptions.forEach { size ->
                            val isSelected = selectedPreviewSize == size
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) primary_light else whiteBg,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Primary else grey_border
                                ),
                                modifier = Modifier.clickable { selectedPreviewSize = size }
                            ) {
                                Text(
                                    text = size,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Primary else headerGrey,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 2. General Information Accordion Card ──
        item {
            FormAccordionCard(
                title = "General Information",
                isRoundedCorner = true,
                icon = Icons.Default.Info,
                expanded = generalInfoExpanded,
                onHeaderClick = { generalInfoExpanded = !generalInfoExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PRODUCT CATEGORY", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (category.isNotBlank() && category != "Select an option") category else "Apparel > Men > Shirts",
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BRAND", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = brand.ifBlank { "Cuso Tailor" },
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = grey_border)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SKU BASE", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = sku.ifBlank { "CT-SH-PC-2024" },
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("STATUS", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "● Draft",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Description Accordion Card ──
        item {
            FormAccordionCard(
                title = "Description",
                isRoundedCorner = true,
                icon = Icons.Default.Description,
                expanded = descriptionExpanded,
                onHeaderClick = { descriptionExpanded = !descriptionExpanded }
            ) {
                Text(
                    text = "A versatile essential for the modern wardrobe, crafted from 100% long-staple cotton for breathability and a crisp finish.",
                    fontSize = tokens.bodySmall,
                    color = headerGrey,
                    lineHeight = (tokens.bodySmall.value * 1.35f).sp
                )
            }
        }

        // ── 4. Variant Summary Accordion Card ──
        item {
            FormAccordionCard(
                title = "Variant Summary",
                isRoundedCorner = true,
                icon = Icons.Default.Layers,
                expanded = variantSummaryExpanded,
                onHeaderClick = { variantSummaryExpanded = !variantSummaryExpanded }
            ) {
                Text(
                    text = "Total 24 combinations across 6 Sizes (XS–XXL) and 4 Colors.",
                    fontSize = tokens.bodySmall,
                    color = headerGrey
                )
            }
        }

        // ── 5. Pricing Accordion Card ──
        item {
            FormAccordionCard(
                title = "Pricing",
                isRoundedCorner = true,
                icon = Icons.Default.AttachMoney,
                expanded = pricingExpanded,
                onHeaderClick = { pricingExpanded = !pricingExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Selling Price", fontSize = tokens.bodySmall, color = headerGrey)
                    Text(
                        text = if (sellingPrice.isNotBlank()) "₹$sellingPrice.00" else "$45.00",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tax Class", fontSize = tokens.bodySmall, color = headerGrey)
                    Text("Standard (15%)", fontSize = tokens.bodySmall, color = TextPrimary)
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Tiered Pricing",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(badgeGrey, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10–49 units", fontSize = tokens.caption, color = headerGrey)
                        Text("$40.50", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50+ units", fontSize = tokens.caption, color = headerGrey)
                        Text("$36.00", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }
        }

        // ── 6. Inventory Accordion Card ──
        item {
            FormAccordionCard(
                title = "Inventory",
                isRoundedCorner = true,
                icon = Icons.Default.Inventory2,
                expanded = inventoryExpanded,
                onHeaderClick = { inventoryExpanded = !inventoryExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Total Stock", fontSize = tokens.bodySmall, color = headerGrey)
                    Text(
                        text = if (openingStock.isNotBlank()) "$openingStock Units" else "120 Units",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Low Stock Alert", fontSize = tokens.bodySmall, color = headerGrey)
                    Text("10 Units", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = redText)
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Distribution",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(badgeGrey, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Main Warehouse", fontSize = tokens.caption, color = headerGrey)
                        Text("85 Units", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("East Side Branch", fontSize = tokens.caption, color = headerGrey)
                        Text("35 Units", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                    }
                }
            }
        }
    }
}

/** STEP 9: Publish */
@Composable
private fun StepPublish(
    tokens: AppDesignTokens,
    publishOption: String, onPublishOptionChange: (String) -> Unit,
    visibilityOption: String, onVisibilityOptionChange: (String) -> Unit,
    productName: String,
    category: String
) {
    var publishExpanded by remember { mutableStateOf(true) }
    var visibilityExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = tokens.screenPadding),
        contentPadding = PaddingValues(top = tokens.screenPadding, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
    ) {
        item {
            FormAccordionCard(
                title = "Publishing Options",
                isRoundedCorner = true,
                iconPainter = R.drawable.ic_plane,
                expanded = publishExpanded,
                onHeaderClick = { publishExpanded = !publishExpanded }
            ) {
                listOf(
                    "Publish Now" to "Make your product immediately visible to all customers.",
                    "Schedule Publishing" to "Choose a future date and time for automatic launch.",
                    "Save as Draft" to "Store it securely in your workspace for later finalization."
                ).forEach { (opt, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onPublishOptionChange(opt) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Spacer(Modifier.width(8.dp))
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppRadioButton(
                                    selected = publishOption == opt,
                                    onClick = { onPublishOptionChange(opt) }
                                )
                                Text(
                                    opt,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                            Text(desc, fontSize = tokens.caption, color = headerGrey)
                        }
                    }
                }
            }
        }

        item {
            FormAccordionCard(
                title = "Product Visibility",
                isRoundedCorner = true,
                icon = Icons.Default.Visibility,
                expanded = visibilityExpanded,
                onHeaderClick = { visibilityExpanded = !visibilityExpanded }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(
                        "Public" to Icons.Default.Public,
                        "Private" to Icons.Default.Lock,
                        "Hidden" to Icons.Default.VisibilityOff
                    ).forEach { (v, icon) ->
                        val isSelected = visibilityOption == v
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) primary_light else whiteBg, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) Primary else grey_border, RoundedCornerShape(8.dp))
                                .clickable { onVisibilityOptionChange(v) }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = null, tint = if (isSelected) Primary else headerGrey, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(v, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = if (isSelected) Primary else TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 4. COMMON REUSABLE COMPONENTS
// ==============================================================================

@Composable
private fun ChannelToggleRow(
    tokens: AppDesignTokens,
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(primary_light, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(subtitle, fontSize = tokens.caption, color = headerGrey)
            }
        }
        MiniSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PriceInputField(
    value: String,
    onValueChange: (String) -> Unit,
    tokens: AppDesignTokens,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .padding(horizontal = tokens.cardPadding * 0.6f),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "₹ ",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = headerGrey
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                textStyle = TextStyle(
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}