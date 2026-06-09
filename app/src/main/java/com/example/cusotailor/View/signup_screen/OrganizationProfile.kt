package com.example.cusotailor.View.signup_screen

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
import com.example.cusotailor.View.composable.AppLogo  // ✅ fixed import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationProfile() {

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
        AppLogo()

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
            OrgLabel("Organization Name")
            OrgTextField(value = organizationName, onValueChange = { organizationName = it })

            Spacer(modifier = Modifier.height(12.dp))

            // ORGANIZATION TYPE
            OrgLabel("Organization Type")
            DropdownSimple( orgTypes, organizationType) { organizationType = it }

            Spacer(modifier = Modifier.height(12.dp))

            // BUSINESS TYPE
            OrgLabel("Business Type")
            DropdownSimple(businessTypes, businessType) { businessType = it }

            Spacer(modifier = Modifier.height(12.dp))

            // COMPANY SIZE
            OrgLabel("Company Size")
            DropdownSimple(companySizes, companySize) { companySize = it }

            Spacer(modifier = Modifier.height(12.dp))

            // SEGMENT
            OrgLabel("Segment")
            DropdownSimple(segments, segment) { segment = it }

            Spacer(modifier = Modifier.height(12.dp))

            // COUNTRY
            OrgLabel("Country / Region")
            OrgTextField(value = country, onValueChange = { country = it })

            Spacer(modifier = Modifier.height(12.dp))

            // ADDRESS
            OrgLabel("Address")
            OrgTextField(value = address, onValueChange = { address = it })

            Spacer(modifier = Modifier.height(12.dp))

            // CITY
            OrgLabel("City")
            OrgTextField(value = city, onValueChange = { city = it })

            Spacer(modifier = Modifier.height(12.dp))

            // PINCODE
            OrgLabel("Pincode / Zip / Postal")
            OrgTextField(value = pincode, onValueChange = { pincode = it })

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

            OrgLabel("Currency")
            DropdownSimple(currencies, currency) { currency = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Language")
            DropdownSimple(languages, language) { language = it }

            Spacer(modifier = Modifier.height(12.dp))

            OrgLabel("Timezone")
            DropdownSimple(timezones, timezone) { timezone = it }

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

            OrgLabel("GST")
            OrgTextField(value = gst, onValueChange = { gst = it })

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ✅ Reusable label with black color
@Composable
fun OrgLabel(text: String) {
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
fun OrgTextField(value: String, onValueChange: (String) -> Unit) {
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
fun DropdownSimple(
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray,
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = Color.Black) },  // ✅ black text in dropdown
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }
}