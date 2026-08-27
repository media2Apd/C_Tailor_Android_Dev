package com.cuso.mobile.view.organization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.OrgSettingsRequest
import com.cuso.mobile.model.organizationSetUpRequest
import com.cuso.mobile.view.composable.AppLogo
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.CountryAndStatePicker
import com.cuso.mobile.viewmodel.Authenticate
import com.cuso.mobile.viewmodel.UiState
import java.util.Currency
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.whiteBg
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OrganizationProfile(
    authViewModel: Authenticate = hiltViewModel(),
    onSetupComplete: () -> Unit   // ← navigates to home on success
) {
    val organization by authViewModel.organization.collectAsStateWithLifecycle()
    val settings by authViewModel.settings.collectAsStateWithLifecycle()
    val accountState by authViewModel.accountState.collectAsStateWithLifecycle()

    var organizationName by rememberSaveable { mutableStateOf("") }
    var organizationType by rememberSaveable { mutableStateOf("") }
    var businessType by rememberSaveable { mutableStateOf("") }
    var companySize by rememberSaveable { mutableStateOf("") }
    var segment by rememberSaveable { mutableStateOf("") }
    var selectedCountry by rememberSaveable { mutableStateOf("") }
    var selectedState by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var pincode by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("") }
    var timezone by rememberSaveable { mutableStateOf("") }
    var taxEnabled by rememberSaveable { mutableStateOf(false) }
    var gst by rememberSaveable { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // ── Organization Logo state ──
    var selectedLogoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var capturedLogoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val logoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedLogoUri = it }
    }

    val logoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            capturedLogoUri?.let { uri ->
                selectedLogoUri = uri
                capturedLogoUri = null
            }
        }
    }

    fun captureLogoImage() {
        if (cameraPermissionState.status.isGranted) {
            val tempFile = File.createTempFile("org_logo_", ".jpg", context.cacheDir)
            capturedLogoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            capturedLogoUri?.let { logoCameraLauncher.launch(it) }
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isSubmitting = accountState is UiState.Loading

    // React to the API result from authViewModel.organizationSetup(...)
    LaunchedEffect(accountState) {
        when (val state = accountState) {
            is UiState.Success -> {
                authViewModel.resetState()
                onSetupComplete()
            }
            is UiState.Error -> {
                errorMessage = state.message
            }
            else -> Unit
        }
    }

    // Auto-fill from Room once the saved organization loads.
    // rememberSaveable means this only overwrites the field on first load
    // (organizationName stays empty until then), not on every recomposition.
    LaunchedEffect(organization) {
        organization?.let { org ->
            if (organizationName.isEmpty()) {
                organizationName = org.name
            }
        }
        settings?.let { setting ->
            if (selectedCountry.isEmpty()) {
                selectedCountry = setting.country ?: ""
            }
            if (selectedState.isEmpty()) {
                selectedState = setting.state ?: ""
            }
            if (currency.isEmpty()) {
                currency = setting.currency ?: ""
            }
            if (timezone.isEmpty()) {
                timezone = setting.timezone ?: ""
            }
        }
    }

    val orgTypes = listOf(
        "Sole Proprietorship", "Partnership Firm",
        "Limited Liability Partnership (LLP)", "Private Limited Company (Pvt Ltd)",
        "Public Limited Company", "One Person Company (OPC)",
        "Section 8 Company / NGO", "Trust / Society",
        "Civil / Local Body", "Other"
    )
    val businessTypes = listOf(
        "Bespoke Tailoring", "Alternations & Repairs",
        "Apparel Retailer", "Uniform & Corporate Wear", "Other"
    )
    val companySizes = listOf(
        "1-10 employees", "11-50 employees", "51-200 employees",
        "201-500 employees", "500+ employees"
    )
    val segments = listOf("B2B", "B2C", "Both")

    val currencies = remember {
        Currency.getAvailableCurrencies()
            .sortedBy { it.currencyCode }
            .map { "${it.currencyCode} - ${it.displayName}" }
    }

    val languages = remember {
        Locale.getAvailableLocales()
            .map { it.displayLanguage }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val timezones = remember {
        TimeZone.getAvailableIDs().map { id ->
            val tz = TimeZone.getTimeZone(id)
            val offsetHours = tz.rawOffset / 3600000
            val sign = if (offsetHours >= 0) "+" else ""
            "$id (UTC$sign$offsetHours)"
        }.sorted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo()

        OrgLabel("Organization Logo")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { logoGalleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, grey_border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    null,
                    tint = Color(0xFF3B3BF9),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Browse Files", fontSize = 13.sp, color = Color(0xFF374151))
            }

            OutlinedButton(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        captureLogoImage()
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, grey_border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    null,
                    tint = Color(0xFF3B3BF9),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Camera", fontSize = 13.sp, color = Color(0xFF374151))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedLogoUri != null) {
            Box(
                modifier = Modifier.size(90.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedLogoUri),
                        contentDescription = "Organization Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                        .clickable { selectedLogoUri = null },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = whiteBg,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(whiteBg, RoundedCornerShape(8.dp))
                    .border(1.dp, grey_border, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No logo added",
                    fontSize = 13.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFF2F2F2))
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Column(Modifier.padding(20.dp)) {

            // ── Organizational Details ──
            Text(
                "Organizational Details",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                color = blackTitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Organization Name")
            OrgTextField(
                value = organizationName,
                onValueChange = { organizationName = it },
                enabled = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Organization Type")
            OrganizationDropdown(orgTypes, organizationType) { organizationType = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Business Type")
            OrganizationDropdown(businessTypes, businessType) { businessType = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Company Size")
            OrganizationDropdown(companySizes, companySize) { companySize = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Segment")
            OrganizationDropdown(segments, segment) { segment = it }

            Spacer(modifier = Modifier.height(12.dp))

            CountryAndStatePicker(
                selectedCountry = selectedCountry,
                selectedState = selectedState,
                onCountryChange = { selectedCountry = it },
                onStateChange = { selectedState = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Address")
            OrgTextField(value = address, onValueChange = { address = it })

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("City")
            OrgTextField(value = city, onValueChange = { city = it })

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Pincode / Zip / Postal")
            OrgTextField(value = pincode, onValueChange = { pincode = it })

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(16.dp))

            // ── Regional Settings ──
            Text(
                "Regional Settings",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = blackTitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Currency")
            OrganizationDropdown(currencies, currency) { currency = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Language")
            OrganizationDropdown(languages, language) { language = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Timezone")
            OrganizationDropdown(timezones, timezone) { timezone = it }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(16.dp))

            // ── Tax Information ──
            Text(
                "Tax Information",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = blackTitle
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Is this bussiness registered  for GST/VAT/TRN/Local Tax ?", color = Color.Gray, fontSize = 15.sp)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = taxEnabled,
                    onCheckedChange = { taxEnabled = it },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = whiteBg,
                        checkedTrackColor = Color.Green
                    )
                )
            }

            if (taxEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                OrgLabel("GST")
                OrgTextField(value = gst, onValueChange = { gst = it })

                HorizontalDivider(color = Color(0xFFF2F2F2))
                Spacer(modifier = Modifier.height(15.dp))
            }
            HorizontalDivider(color = Color(0xFFF2F2F2))
            Spacer(modifier = Modifier.height(12.dp))

            // ← now driven by the outer isChecked, not its own private copy
            TermsScreen(
                isChecked = isChecked,
                onCheckedChange = { isChecked = it }
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    errorMessage = null
                    when {
                        organizationType.isBlank() -> errorMessage = "Select organization type"
                        businessType.isBlank()     -> errorMessage = "Select business type"
                        companySize.isBlank()      -> errorMessage = "Select company size"
                        segment.isBlank()          -> errorMessage = "Select segment"
                        selectedCountry.isBlank()  -> errorMessage = "Select country"
                        selectedState.isBlank()    -> errorMessage = "Select state"
                        address.isBlank()          -> errorMessage = "Enter address"
                        city.isBlank()             -> errorMessage = "Enter city"
                        pincode.isBlank()          -> errorMessage = "Enter pincode"
                        currency.isBlank()         -> errorMessage = "Select currency"
                        language.isBlank()         -> errorMessage = "Select language"
                        timezone.isBlank()         -> errorMessage = "Select timezone"
                        taxEnabled && gst.isBlank() -> errorMessage = "Enter your tax ID"
                        else -> {
                            val currencyCode = currency.substringBefore(" - ")
                            val timezoneId = timezone.substringBefore(" (")

                            authViewModel.organizationSetup(
                                organizationSetUpRequest(
                                    orgType = organizationType,
                                    businessType = businessType,
                                    settings = OrgSettingsRequest(
                                        companySize = companySize,
                                        country = selectedCountry,
                                        state = selectedState,
                                        timezone = timezoneId,
                                        currency = currencyCode,
                                        language = language,
                                        marketingEmails = isChecked,
                                        address = address,
                                        city = city,
                                        pincode = pincode
                                    ),
                                    segments = listOf(segment),
                                    isTaxId = taxEnabled,
                                    taxId = if (taxEnabled) gst else ""
                                )
                            )
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = whiteBg
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    CirculerProgressIndicatorReuse()

                } else {
                    Text("Get Started")
                }
            }
        }
    }
}

@Composable
fun TermsScreen(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Blue,
                    uncheckedColor = Color.Gray,
                    checkmarkColor = whiteBg,
                    disabledCheckedColor = Color.LightGray,
                    disabledUncheckedColor = Color.LightGray
                )
            )

            Text(
                text = "I'd like to receive marketing emails about product updates and special offers",
                color = blackTitle, fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ── Label ──
@Composable
fun OrgLabel(text: String) {
    Text(
        text = text,
        color = blackTitle,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

// ── Text Field ──
@Composable
fun OrgTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Blue,
            unfocusedTextColor = blackTitle,
            disabledTextColor = blackTitle,
            disabledBorderColor = Color.LightGray,
            disabledContainerColor = Color(0xFFF2F2F2),
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = blackTitle,
            unfocusedContainerColor = Color(0xFFF2F2F2),
            focusedContainerColor = whiteBg
        )
    )
}

// ── Dropdown ──
@Composable
fun OrganizationDropdown(
    items: List<String>,
    selected: String,
    enabled: Boolean = true,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isEmpty()) items
        else items.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column {
        // Trigger field — now matches FormDropdown's look exactly
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    if (enabled) whiteBg else Color(0xFFF3F4F6),
                    RoundedCornerShape(8.dp)
                )
                .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected.ifEmpty { "Select an option" },
                fontSize = 12.sp,
                color = when {
                    !enabled -> Color(0xFF9CA3AF)
                    selected.isEmpty() -> Color(0xFF9CA3AF)
                    else -> Color(0xFF374151)
                }
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (enabled) Color.Gray else Color(0xFFD1D5DB)
            )
        }

        // Dropdown card — only ever shown if enabled AND expanded
        AnimatedVisibility(visible = expanded && enabled) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search...", color = Color.Gray, fontSize = 14.sp)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .focusRequester(focusRequester),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = blackTitle,
                            unfocusedTextColor = blackTitle,
                            focusedBorderColor = Color.Blue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = whiteBg,
                            unfocusedContainerColor = whiteBg,
                            cursorColor = blackTitle
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    HorizontalDivider(color = Color.LightGray)

                    LazyColumn {
                        if (filteredItems.isEmpty()) {
                            item {
                                Text(
                                    "No results found",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(filteredItems) { item ->
                                Text(
                                    text = item,
                                    color = blackTitle,
                                    fontSize = 15.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelect(item)
                                            expanded = false
                                            searchQuery = ""
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(expanded) {
            if (expanded) {
                delay(150.milliseconds)
                focusRequester.requestFocus()
            } else {
                searchQuery = ""
            }
        }
    }
}