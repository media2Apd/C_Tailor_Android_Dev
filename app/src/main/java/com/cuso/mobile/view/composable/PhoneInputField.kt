package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.model.countries
import com.cuso.mobile.model.Country
import com.cuso.mobile.view.home.FormLabel
@Composable
fun PhoneInputField(
    phoneValue: String,
    onPhoneChange: (String) -> Unit,
    onCountryChange: (Country) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember {
        mutableStateOf(
            countries.firstOrNull { it.iso == "IN" } ?: countries.first()
        )
    }

    Column {
        FormLabel("Mobile Number")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                .background(
                    if (enabled) Color.White else Color(0xFFF2F2F2),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Country picker ──
            Box {
                Row(
                    modifier = Modifier
                        .then(
                            if (enabled) Modifier.clickable { expanded = true }
                            else Modifier
                        )
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedCountry.flag, fontSize = 16.sp)
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(18.dp)
                            .background(Color(0xFFD1D5DB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedCountry.code,
                        color = if (enabled) Color(0xFF374151) else Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }

                DropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .height(400.dp)
                        .background(Color.White)
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${country.flag} ${country.name} (${country.code})",
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedCountry = country
                                onCountryChange(country)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Phone number input ──
            BasicTextField(
                value = phoneValue,
                onValueChange = { input ->
                    val digitsOnly = input.filter { it.isDigit() }
                    if (digitsOnly.length <= 15) onPhoneChange(digitsOnly)
                },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = if (enabled) Color(0xFF374151) else Color(0xFF9CA3AF),
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(Color(0xFF374151)),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (phoneValue.isEmpty()) {
                            Text(
                                "Enter phone number",
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}