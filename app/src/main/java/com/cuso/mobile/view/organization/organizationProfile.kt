package com.cuso.mobile.view.organization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.view.composable.appLogo
import com.cuso.mobile.view.composable.countryAndStatePicker
import com.cuso.mobile.viewmodel.Authenticate
import java.util.Currency
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun organizationProfile(
    authViewModel: Authenticate = hiltViewModel()
) {
    val organization by authViewModel.organization.collectAsStateWithLifecycle()

    var country by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }

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
    var showCheckbox by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }

    // Auto-fill from Room once the saved organization loads.
    // rememberSaveable means this only overwrites the field on first load
    // (organizationName stays empty until then), not on every recomposition.
    LaunchedEffect(organization) {
        organization?.let { org ->
            if (organizationName.isEmpty()) {
                organizationName = org.name
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
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        appLogo()

        Spacer(modifier = Modifier.height(16.dp))

        Column(Modifier.padding(20.dp)) {

            // ── Organizational Details ──
            Text(
                "Organizational Details",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                color = Color.Black
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

            countryAndStatePicker(
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
                color = Color.Black
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
                color = Color.Black
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
                    modifier=Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
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

            TermsScreen()
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
@Composable
fun TermsScreen() {
    var isChecked by remember { mutableStateOf(false) }

    Column {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Blue,
                    uncheckedColor = Color.Gray,
                    checkmarkColor = Color.White,
                    disabledCheckedColor = Color.LightGray,
                    disabledUncheckedColor = Color.LightGray
                )
            )

            Text(
                text = "I'd like to receive marketing emails about product updates and special offers",
                color = Color.Black, fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Navigate or perform action
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Get Started")
        }
    }
}
// ── Label ──
@Composable
fun OrgLabel(text: String) {
    Text(
        text = text,
        color = Color.Black,
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
            unfocusedTextColor = Color.Black,
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.LightGray,
            disabledContainerColor = Color(0xFFF2F2F2),
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.LightGray,
            cursorColor = Color.Black,
            unfocusedContainerColor = Color(0xFFF2F2F2),
            focusedContainerColor = Color.White
        )
    )
}

// ── Dropdown ──
@Composable
fun OrganizationDropdown(
    items: List<String>,
    selected: String,
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
        // Trigger field
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                    else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if (selected.isEmpty()) Color.Gray else Color.Black,
                disabledBorderColor = Color.LightGray,
                disabledContainerColor = Color(0xFFF2F2F2),
                disabledTrailingIconColor = Color.LightGray
            )
        )

        // Dropdown card
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Search field
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
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color.Blue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    HorizontalDivider(color = Color.LightGray)

                    // Items list
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
                                    color = Color.Black,
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

        // Auto focus search when opened
        LaunchedEffect(expanded) {
            if (expanded) {
                delay(150)
                focusRequester.requestFocus()
            } else {
                searchQuery = ""
            }
        }

    }
}