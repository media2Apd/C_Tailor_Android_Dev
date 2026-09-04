@file:Suppress("UNUSED_PARAMETER", "unused")

package com.cuso.mobile.view.home.profile_settings.setup_pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.ImageUploadSection
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction

data class BusinessSetupData(
    val businessName: String = "",
    val legalBusinessName: String = "",
    val tradeName: String = "",
    val industry: String = "",
    val businessType: String = "",
    val pan: String = "",
    val brn: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "India",
    val pinCode: String = "",
    val businessPhone: String = "",
    val businessEmail: String = "",
    val website: String = "",
    val logos: List<Uri> = emptyList()
)

@Composable
fun BusinessSetupScreen(
    initialData: BusinessSetupData = BusinessSetupData(),
    isLoading: Boolean = false,
    onClose: () -> Unit = {},
    onSaveAndContinue: (BusinessSetupData) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Accordion active expanded section
    var expandedSection by remember { mutableStateOf("info") }

    // Section 1: Business Information
    var businessName by remember { mutableStateOf(initialData.businessName) }
    var legalBusinessName by remember { mutableStateOf(initialData.legalBusinessName) }
    var tradeName by remember { mutableStateOf(initialData.tradeName) }
    var industry by remember { mutableStateOf(initialData.industry) }
    var industryExpanded by remember { mutableStateOf(false) }
    var businessType by remember { mutableStateOf(initialData.businessType) }
    var businessTypeExpanded by remember { mutableStateOf(false) }

    // Section 2: Business Identification
    var pan by remember { mutableStateOf(initialData.pan) }
    var brn by remember { mutableStateOf(initialData.brn) }

    // Section 3: Business Address
    var addressLine1 by remember { mutableStateOf(initialData.addressLine1) }
    var addressLine2 by remember { mutableStateOf(initialData.addressLine2) }
    var city by remember { mutableStateOf(initialData.city) }
    var state by remember { mutableStateOf(initialData.state) }
    var country by remember { mutableStateOf(initialData.country.ifBlank { "India" }) }
    var pinCode by remember { mutableStateOf(initialData.pinCode) }

    // Section 4: Contact Information
    var businessPhone by remember { mutableStateOf(initialData.businessPhone) }
    var businessEmail by remember { mutableStateOf(initialData.businessEmail) }
    var website by remember { mutableStateOf(initialData.website) }

    // Section 5: Business Logo
    var selectedLogos by remember { mutableStateOf(initialData.logos) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedLogos = selectedLogos + uris
        }
    }

    // Validation State
    var businessNameError by remember { mutableStateOf(false) }
    var legalBusinessNameError by remember { mutableStateOf(false) }
    var panError by remember { mutableStateOf(false) }
    var addressLine1Error by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf(false) }
    var stateError by remember { mutableStateOf(false) }
    var pinCodeError by remember { mutableStateOf(false) }
    var businessPhoneError by remember { mutableStateOf(false) }
    var businessEmailError by remember { mutableStateOf(false) }

    val industryOptions = listOf(
        "Tailoring & Apparel Customization",
        "Fashion & Clothing Retail",
        "Textile Manufacturing",
        "Garment Export"
    )

    val businessTypeOptions = listOf(
        "Proprietorship",
        "Partnership",
        "Private Limited",
        "Limited Liability Partnership (LLP)"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = "Business Setup",
                    onClose = onClose
                )
                HorizontalDivider(color = title_border)
            }

            // Accordion Sections Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2f)
            ) {
                // 1. Business Information Section
                AccordionSection(
                    title = "Business Information",
                    expanded = expandedSection == "info",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "info") "" else "info"
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
                    ) {
                        Column {
                            FormLabel("Business Name", isRequired = true)
                            FormTextField(
                                value = businessName,
                                onValueChange = {
                                    businessName = it
                                    businessNameError = false
                                },
                                placeholder = "e.g. Elegant Stitches",
                                isError = businessNameError,
                                errorMessage = if (businessNameError) "Business Name is required" else null
                            )
                        }

                        Column {
                            FormLabel("Legal Business Name", isRequired = true)
                            FormTextField(
                                value = legalBusinessName,
                                onValueChange = {
                                    legalBusinessName = it
                                    legalBusinessNameError = false
                                },
                                placeholder = "e.g. Elegant Stitches Private Limited",
                                isError = legalBusinessNameError,
                                errorMessage = if (legalBusinessNameError) "Legal Business Name is required" else null
                            )
                        }

                        Column {
                            FormLabel("Trade Name")
                            FormTextField(
                                value = tradeName,
                                onValueChange = { tradeName = it },
                                placeholder = "e.g. Elegant Stitches Brand"
                            )
                        }

                        Column {
                            FormDropdown(
                                label = "Industry",
                                value = industry.ifBlank { "Tailoring & Apparel Customization" },
                                expanded = industryExpanded,
                                onExpandChange = { industryExpanded = it },
                                options = industryOptions,
                                onOptionSelected = { industry = it },
                                isRequired = true
                            )
                        }

                        Column {
                            FormDropdown(
                                label = "Business Type",
                                value = businessType.ifBlank { "Proprietorship" },
                                expanded = businessTypeExpanded,
                                onExpandChange = { businessTypeExpanded = it },
                                options = businessTypeOptions,
                                onOptionSelected = { businessType = it },
                                isRequired = true
                            )
                        }
                    }
                }

                // 2. Business Identification Section
                AccordionSection(
                    title = "Business Identification",
                    expanded = expandedSection == "identification",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "identification") "" else "identification"
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
                    ) {
                        Column {
                            FormLabel("PAN", isRequired = true)
                            FormTextField(
                                value = pan,
                                onValueChange = {
                                    pan = it.uppercase()
                                    panError = false
                                },
                                placeholder = "e.g. ABCDE1234F",
                                isError = panError,
                                errorMessage = if (panError) "PAN is required" else null
                            )
                        }

                        Column {
                            FormLabel("Business Registration Number (BRN)")
                            FormTextField(
                                value = brn,
                                onValueChange = { brn = it },
                                placeholder = "e.g. U12345MH2021PTC123456"
                            )
                        }
                    }
                }

                // 3. Business Address Section
                AccordionSection(
                    title = "Business Address",
                    expanded = expandedSection == "address",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "address") "" else "address"
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
                    ) {
                        Column {
                            FormLabel("Address Line 1", isRequired = true)
                            FormTextField(
                                value = addressLine1,
                                onValueChange = {
                                    addressLine1 = it
                                    addressLine1Error = false
                                },
                                placeholder = "e.g. Flat/Room No, Building Name, Street Address",
                                isError = addressLine1Error,
                                errorMessage = if (addressLine1Error) "Address Line 1 is required" else null
                            )
                        }

                        Column {
                            FormLabel("Address Line 2 (Optional)")
                            FormTextField(
                                value = addressLine2,
                                onValueChange = { addressLine2 = it },
                                placeholder = "e.g. Landmark, Locality, Area"
                            )
                        }

                        Column {
                            FormLabel("City", isRequired = true)
                            FormTextField(
                                value = city,
                                onValueChange = {
                                    city = it
                                    cityError = false
                                },
                                placeholder = "e.g. Mumbai",
                                isError = cityError,
                                errorMessage = if (cityError) "City is required" else null
                            )
                        }

                        Column {
                            FormLabel("State", isRequired = true)
                            FormTextField(
                                value = state,
                                onValueChange = {
                                    state = it
                                    stateError = false
                                },
                                placeholder = "Maharashtra",
                                isError = stateError,
                                errorMessage = if (stateError) "State is required" else null
                            )
                        }

                        Column {
                            FormLabel("Country", isRequired = true)
                            FormTextField(
                                value = country,
                                onValueChange = { country = it },
                                placeholder = "India"
                            )
                        }

                        Column {
                            FormLabel("PIN Code", isRequired = true)
                            FormTextField(
                                value = pinCode,
                                onValueChange = {
                                    pinCode = it
                                    pinCodeError = false
                                },
                                placeholder = "e.g. 400001",
                                keyboardType = KeyboardType.Number,
                                isError = pinCodeError,
                                errorMessage = if (pinCodeError) "PIN Code is required" else null
                            )
                        }
                    }
                }

                // 4. Contact Information Section
                AccordionSection(
                    title = "Contact Information",
                    expanded = expandedSection == "contact",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "contact") "" else "contact"
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
                    ) {
                        Column {
                            FormLabel("Business Phone", isRequired = true)
                            FormTextField(
                                value = businessPhone,
                                onValueChange = {
                                    businessPhone = it
                                    businessPhoneError = false
                                },
                                placeholder = "e.g. +91 98765 43210",
                                keyboardType = KeyboardType.Phone,
                                isError = businessPhoneError,
                                errorMessage = if (businessPhoneError) "Business Phone is required" else null
                            )
                        }

                        Column {
                            FormLabel("Business Email", isRequired = true)
                            FormTextField(
                                value = businessEmail,
                                onValueChange = {
                                    businessEmail = it
                                    businessEmailError = false
                                },
                                placeholder = "e.g. contact@elegantstitches.com",
                                keyboardType = KeyboardType.Email,
                                isError = businessEmailError,
                                errorMessage = if (businessEmailError) "Business Email is required" else null
                            )
                        }

                        Column {
                            FormLabel("Website (Optional)")
                            FormTextField(
                                value = website,
                                onValueChange = { website = it },
                                placeholder = "https://elegantstitches.com",
                                keyboardType = KeyboardType.Uri
                            )
                        }
                    }
                }

                // 5. Business Logo Section
                AccordionSection(
                    title = "Business Logo",
                    expanded = expandedSection == "logo",
                    onHeaderClick = {
                        expandedSection = if (expandedSection == "logo") "" else "logo"
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        ImageUploadSection(
                            isImage = false,
                            selectedImages = selectedLogos,
                            onBrowseClick = { imagePickerLauncher.launch("image/*") },
                            onRemoveImage = { uri ->
                                selectedLogos = selectedLogos.filter { it != uri }
                            },
                            documentUploadText = "Supported formats: PNG, JPG, SVG (Max. 3MB) · Recommended: 512x512px",
                            uploadBoxHeight = tokens.cardHeight * 1.2f
                        )
                    }
                }
            }
        }

        // Floating Bottom Actions (Cancel & Save & Continue)
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Update(
                isLoading = isLoading,
                label = "Save & Continue",
                onClick = {
                    var hasError = false

                    if (businessName.isBlank()) {
                        businessNameError = true
                        hasError = true
                        expandedSection = "info"
                    }
                    if (legalBusinessName.isBlank()) {
                        legalBusinessNameError = true
                        hasError = true
                        if (!businessNameError) expandedSection = "info"
                    }
                    if (pan.isBlank()) {
                        panError = true
                        hasError = true
                        if (!hasError) expandedSection = "identification"
                    }
                    if (addressLine1.isBlank()) {
                        addressLine1Error = true
                        hasError = true
                        if (!hasError) expandedSection = "address"
                    }
                    if (city.isBlank()) {
                        cityError = true
                        hasError = true
                        if (!hasError) expandedSection = "address"
                    }
                    if (state.isBlank()) {
                        stateError = true
                        hasError = true
                        if (!hasError) expandedSection = "address"
                    }
                    if (pinCode.isBlank()) {
                        pinCodeError = true
                        hasError = true
                        if (!hasError) expandedSection = "address"
                    }
                    if (businessPhone.isBlank()) {
                        businessPhoneError = true
                        hasError = true
                        if (!hasError) expandedSection = "contact"
                    }
                    if (businessEmail.isBlank()) {
                        businessEmailError = true
                        hasError = true
                        if (!hasError) expandedSection = "contact"
                    }

                    if (hasError) return@Update

                    onSaveAndContinue(
                        BusinessSetupData(
                            businessName = businessName,
                            legalBusinessName = legalBusinessName,
                            tradeName = tradeName,
                            industry = industry.ifBlank { "Tailoring & Apparel Customization" },
                            businessType = businessType.ifBlank { "Proprietorship" },
                            pan = pan,
                            brn = brn,
                            addressLine1 = addressLine1,
                            addressLine2 = addressLine2,
                            city = city,
                            state = state,
                            country = country,
                            pinCode = pinCode,
                            businessPhone = businessPhone,
                            businessEmail = businessEmail,
                            website = website,
                            logos = selectedLogos
                        )
                    )
                }
            )
        )
    }
}