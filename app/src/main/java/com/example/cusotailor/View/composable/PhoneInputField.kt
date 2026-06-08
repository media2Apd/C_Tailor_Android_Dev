package com.example.cusotailor.View.composable

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
import com.example.cusotailor.model.countries
import com.example.cusotailor.model.Country
import com.example.cusotailor.model.countries

@Composable
fun PhoneInputField(
    phoneValue: String,
    onPhoneChange: (String) -> Unit,
    onCountryChange: (Country) -> Unit  // ← add this
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }

    Column {
        Text("Phone Number", Modifier, color = Color.Black)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedCountry.flag, fontSize = 18.sp)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                    Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color.Gray))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = selectedCountry.code, color = Color.Gray)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.height(400.dp).fillMaxWidth().background(Color.White)
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${country.flag} ${country.name} (${country.code})",
                                    color = Color.Black
                                )
                            },
                            onClick = {
                                selectedCountry = country
                                onCountryChange(country)  // ← send country to parent
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = phoneValue,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        if (digitsOnly.length <= 15) {  // ← 15 is max international length
                            onPhoneChange(digitsOnly)
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    cursorBrush = SolidColor(Color.Black),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}