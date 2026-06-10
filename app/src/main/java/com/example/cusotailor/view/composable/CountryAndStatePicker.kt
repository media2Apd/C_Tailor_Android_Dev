package com.example.cusotailor.view.composable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cusotailor.model.JsonCountry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream


@Composable
fun CountryAndStatePicker(
    selectedCountry: String,
    selectedState: String,
    onCountryChange: (String) -> Unit,
    onStateChange: (String) -> Unit
) {
    val context = LocalContext.current
    var allCountriesData by remember { mutableStateOf(emptyList<JsonCountry>()) }
    val countryList = remember(allCountriesData) { allCountriesData.map { it.name }.sorted() }
    var stateList by remember { mutableStateOf(emptyList<String>()) }
    var isCountryExpanded by remember { mutableStateOf(false) }
    var isStateExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadJsonFromAssets(context, "countries_data.json")?.let {
            allCountriesData = it
        }
    }

    LaunchedEffect(selectedCountry, allCountriesData) {
        val match = allCountriesData.find { it.name == selectedCountry }
        stateList = match?.states?.map { it.name }?.sorted() ?: emptyList()
    }

    Column() {
        Row {
            Column() {
                Text("Country/Region", color = Color.Black)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCountry.ifEmpty { "Select Region" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        placeholder = { Text("Country/Region", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                tint = Color.Gray
                            )
                        },
                        colors = customFieldColors()
                    )
                    Box(
                        modifier = Modifier.matchParentSize()
                            .clickable { isCountryExpanded = true })
                    DropdownMenu(
                        expanded = isCountryExpanded,
                        onDismissRequest = { isCountryExpanded = false },
                        modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
                            .background(Color.White)
                    ) {
                        countryList.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, color = Color.Black) },
                                onClick = {
                                    onCountryChange(name)   // ← notify parent
                                    onStateChange("")        // ← reset state
                                    isCountryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }


        Spacer(Modifier.padding(top = 5.dp))
        Row() {
            Column() {
                Text("State/Province", color = Color.Black)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedState.ifEmpty { "Select State" },
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        placeholder = { Text("State/Province", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                tint = Color.Gray
                            )
                        },
                        colors = customFieldColors()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        if (stateList.isNotEmpty()) isStateExpanded = true
                    })
                    if (stateList.isNotEmpty()) {
                        DropdownMenu(
                            expanded = isStateExpanded,
                            onDismissRequest = { isStateExpanded = false },
                            modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
                                .background(Color.White)
                        ) {
                            stateList.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name, color = Color.Gray) },
                                    onClick = {
                                        onStateChange(name)   // ← notify parent
                                        isStateExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Updated worker to parse the structured JSON array framework safely
suspend fun loadJsonFromAssets(context: Context, fileName: String): List<JsonCountry>? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<JsonCountry>>() {}.type
            Gson().fromJson<List<JsonCountry>>(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}