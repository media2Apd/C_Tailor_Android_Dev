package com.cuso.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cuso.mobile.R

val AppFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semi_bold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

val Typography = Typography(

    displayLarge = TextStyle(
        fontFamily = AppFontFamily
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily
    ),

    headlineLarge = TextStyle(
        fontFamily = AppFontFamily
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily
    ),

    titleLarge = TextStyle(
        fontFamily = AppFontFamily
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily
    ),

    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = AppFontFamily
    ),

    bodySmall = TextStyle(
        fontFamily = AppFontFamily
    ),

    labelLarge = TextStyle(
        fontFamily = AppFontFamily
    ),

    labelMedium = TextStyle(
        fontFamily = AppFontFamily
    ),

    labelSmall = TextStyle(
        fontFamily = AppFontFamily
    )
)