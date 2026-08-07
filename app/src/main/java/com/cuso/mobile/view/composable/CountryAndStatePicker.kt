package com.cuso.mobile.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.utils.loadJsonFromAssets
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CountryAndStatePicker(
    selectedCountry: String,
    selectedState: String,
    onCountryChange: (String) -> Unit,
    onStateChange: (String) -> Unit
) {
    val context = LocalContext.current
    var allCountriesData by remember { mutableStateOf(emptyList<com.cuso.mobile.model.JsonCountry>()) }
    val countryList = remember(allCountriesData) { allCountriesData.map { it.name }.sorted() }
    var stateList by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        loadJsonFromAssets(context, "countries_data.json")?.let {
            allCountriesData = it
        }
    }

    LaunchedEffect(selectedCountry, allCountriesData) {
        val match = allCountriesData.find { it.name == selectedCountry }
        stateList = match?.states?.map { it.name }?.sorted() ?: emptyList()
    }

    Column {
        // ── Country Picker ──
        Text(
            "Country/Region",
            color = blackTitle,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SearchableDropdownContents(
            items = countryList,
            selected = selectedCountry,
            placeholder = "Select Country",
            onSelect = {
                onCountryChange(it)
                onStateChange("")
            }
        )

        Spacer(Modifier.height(12.dp))

        // ── State Picker ──
        Text(
            "State/Province",
            color = blackTitle,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SearchableDropdownContents(
            items = stateList,
            selected = selectedState,
            placeholder = if (selectedCountry.isEmpty()) "Select Country First" else "Select State",
            enabled = stateList.isNotEmpty(),
            onSelect = { onStateChange(it) }
        )
    }
}

@Composable
fun SearchableDropdownContents(
    items: List<String>,
    selected: String,
    placeholder: String = "Select",
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
        // ── Trigger Field using Box ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)                                    //   height 40dp
                .clickable { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = PrimaryBorder,      //   BorderColor value
                    disabledContainerColor = whiteBg,          //   white background
                    disabledTrailingIconColor = Color.LightGray
                )
            )

            //   Text perfectly centered, no internal padding issues
            Text(
                text = selected.ifEmpty { placeholder },
                color = if (selected.isEmpty()) Color.Gray else blackTitle,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 14.dp, end = 48.dp)
            )
        }

        // ── Dropdown Card ──
        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg)
            ) {
                Column {
                    // ── Search Field ──
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search...", color = Color.Gray, fontSize = 14.sp) },
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

                    // ── Items List ──
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

        // ── Auto Focus Search When Opened ──
        LaunchedEffect(expanded) {
            if (expanded) {
                delay(10.milliseconds)
                focusRequester.requestFocus()
            } else {
                searchQuery = ""
            }
        }
    }
}