package com.cuso.mobile.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.model.sales.JsonCountry
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
    enabled: Boolean = true, // Added enabled parameter
    onCountryChange: (String) -> Unit,
    onStateChange: (String) -> Unit
) {
    val context = LocalContext.current
    var allCountriesData by remember { mutableStateOf(emptyList<JsonCountry>()) }
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
            color = if (enabled) blackTitle else blackTitle.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SearchableDropdownContents(
            items = countryList,
            selected = selectedCountry,
            placeholder = "Select Country",
            enabled = enabled,
            onSelect = {
                onCountryChange(it)
                onStateChange("")
            }
        )

        Spacer(Modifier.height(12.dp))

        // ── State Picker ──
        Text(
            "State/Province",
            color = if (enabled) blackTitle else blackTitle.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SearchableDropdownContents(
            items = stateList,
            selected = selectedState,
            placeholder = if (selectedCountry.isEmpty()) "Select Country First" else "Select State",
            enabled = enabled && stateList.isNotEmpty(),
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

    // ── Dropdown Arrow Rotation Animation ──
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "DropdownArrowRotation"
    )

    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isEmpty()) items
        else items.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 1.dp,
                    color = if (enabled) PrimaryBorder else PrimaryBorder.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                )
                .background(
                    if (enabled) whiteBg else Color(0xFFF9FAFB),
                    RoundedCornerShape(10.dp)
                )
                .clickable(enabled = enabled) { expanded = !expanded }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected.ifEmpty { placeholder },
                    color = when {
                        !enabled -> Color(0xFF6B7280)
                        selected.isEmpty() -> Color.Gray
                        else -> blackTitle
                    },
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (enabled) Color.Gray else Color(0xFFD1D5DB),
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = arrowRotation }
                )
            }
        }

        // ── Dropdown Card ──
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
                    // ── Search Field ──
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search...", color = Color.Gray, fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 8.dp)
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