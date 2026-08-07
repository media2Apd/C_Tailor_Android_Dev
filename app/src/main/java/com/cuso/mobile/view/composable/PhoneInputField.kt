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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.model.countries
import com.cuso.mobile.model.Country
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Refresh
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.whiteBg
import com.google.i18n.phonenumbers.PhoneNumberUtil

//  library instance, no hardcoded data
private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

//  returns max national number digit length for a given ISO country code
fun maxDigitsFor(iso: String): Int {
    return try {
        val example = phoneUtil.getExampleNumber(iso.uppercase())
        example?.nationalNumber?.toString()?.length ?: 15
    } catch (_: Exception) {
        15   // safe fallback for unsupported ISO codes
    }
}



//  inserts "-" after the 5th digit visually, underlying value stays plain digits
class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = if (digits.length <= 5) {
            digits
        } else {
            "${digits.substring(0, 5)}-${digits.substring(5)}"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset <= 5) offset else offset + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 5) offset else (offset - 1).coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun PhoneInputField(
    phoneValue: String,
    onPhoneChange: (String) -> Unit,
    onCountryChange: (Country) -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false ,
    onRetry: () -> Unit = {},
    showRetryButton: Boolean = false,   //  controls whether reload icon shows at all
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember {
        mutableStateOf(
            countries.firstOrNull { it.iso == "IN" } ?: countries.first()
        )
    }

    // per-country max digit limit, derived from libphonenumber (no hardcoding)
    val maxDigits = maxDigitsFor(selectedCountry.iso)

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(
                    1.dp,
                    if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),   // red border on error
                    RoundedCornerShape(8.dp)
                )
                .background(
                    if (enabled) whiteBg else Color(0xFFF2F2F2),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Country picker ── (unchanged)
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
                        .background(whiteBg)
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${country.flag} ${country.name} (${country.code})",
                                    color = blackTitle,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedCountry = country
                                onCountryChange(country)
                                expanded = false
                                //   NEW — trim number if it exceeds new country's max digit length
                                val newMax = maxDigitsFor(country.iso)
                                if (phoneValue.length > newMax) {
                                    onPhoneChange(phoneValue.take(newMax))
                                }
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
                    if (digitsOnly.length <= maxDigits) onPhoneChange(digitsOnly)   //   CHANGED — country-specific cap
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
                visualTransformation = PhoneNumberVisualTransformation(),   //   NEW — shows "-" after 5 digits
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

            // ── Loading indicator (inside the box, trailing end) ──

            if (isLoading) {
                Spacer(modifier = Modifier.width(8.dp))
                CirculerProgressIndicatorSmall()
            } else if (showRetryButton) {          //   CHANGED — only render reload icon when explicitly enabled
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Refresh, null,
                    modifier = Modifier.size(20.dp).clickable { onRetry() },
                    tint = Color.Gray
                )
            }
        }

        //   NEW — inline error message below the field
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color(0xFFEF4444),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}