package com.cuso.mobile.view.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.view.composable.appLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun organizationProfile() {

    var organizationName by rememberSaveable { mutableStateOf("") }
    var organizationType by rememberSaveable { mutableStateOf("") }
    var businessType by rememberSaveable { mutableStateOf("") }
    var companySize by rememberSaveable { mutableStateOf("") }
    var segment by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var pincode by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("") }
    var language by rememberSaveable { mutableStateOf("") }
    var timezone by rememberSaveable { mutableStateOf("") }
    var taxEnabled by rememberSaveable { mutableStateOf(false) }
    var gst by rememberSaveable { mutableStateOf("") }

    val orgTypes = listOf("Startup", "Enterprise", "SME")
    val businessTypes = listOf("Retail", "Manufacturing", "IT", "Service")
    val companySizes = listOf("1-10", "10-50", "50-200", "200+")
    val segments = listOf("B2B", "B2C", "Hybrid")
    val currencies = listOf("USD", "INR", "EUR", "GBP")
    val languages = listOf("English", "Tamil", "Hindi", "French")
    val timezones = listOf("IST", "GMT", "EST", "PST")

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
        Column(
            Modifier.padding(20.dp)
        )
        {// HEADER
            Text(
                "Organization Details",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ORGANIZATION NAME
            orgLabel("Organization Name")
            orgTextField(value = organizationName, onValueChange = { organizationName = it })

            Spacer(modifier = Modifier.height(12.dp))

            // ORGANIZATION TYPE
            orgLabel("Organization Type")
            OrganizationDropdown( orgTypes, organizationType) { organizationType = it }

            Spacer(modifier = Modifier.height(12.dp))

            // BUSINESS TYPE
            orgLabel("Business Type")
            OrganizationDropdown(businessTypes, businessType) { businessType = it }

            Spacer(modifier = Modifier.height(12.dp))

            // COMPANY SIZE
            orgLabel("Company Size")
            OrganizationDropdown(companySizes, companySize) { companySize = it }

            Spacer(modifier = Modifier.height(12.dp))

            // SEGMENT
            orgLabel("Segment")
            OrganizationDropdown(segments, segment) { segment = it }

            Spacer(modifier = Modifier.height(12.dp))

            // COUNTRY
            orgLabel("Country / Region")
            orgTextField(value = country, onValueChange = { country = it })

            Spacer(modifier = Modifier.height(12.dp))

            // ADDRESS
            orgLabel("Address")
            orgTextField(value = address, onValueChange = { address = it })

            Spacer(modifier = Modifier.height(12.dp))

            // CITY
            orgLabel("City")
            orgTextField(value = city, onValueChange = { city = it })

            Spacer(modifier = Modifier.height(12.dp))

            // PINCODE
            orgLabel("Pincode / Zip / Postal")
            orgTextField(value = pincode, onValueChange = { pincode = it })

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = Color.Gray)  // ✅ fixed from Divider

            Spacer(modifier = Modifier.height(16.dp))

            // REGIONAL SETTINGS
            Text(
                "Regional Settings",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            orgLabel("Currency")
            OrganizationDropdown(currencies, currency) { currency = it }

            Spacer(modifier = Modifier.height(12.dp))

            orgLabel("Language")
            OrganizationDropdown(languages, language) { language = it }

            Spacer(modifier = Modifier.height(12.dp))

            orgLabel("Timezone")
            OrganizationDropdown(timezones, timezone) { timezone = it }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = Color.Gray)  // ✅ fixed from Divider

            Spacer(modifier = Modifier.height(16.dp))

            // TAX INFO
            Text(
                "Tax Information",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tax Enable", color = Color.Black, fontSize = 15.sp)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = taxEnabled,
                    onCheckedChange = { taxEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Blue
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            orgLabel("GST")
            orgTextField(value = gst, onValueChange = { gst = it })

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ✅ Reusable label with black color
@Composable
fun orgLabel(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

// ✅ Reusable text field with black text
@Composable
fun orgTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun OrganizationDropdown(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}