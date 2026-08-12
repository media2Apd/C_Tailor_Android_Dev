package com.cuso.mobile.adaptive_screen

import android.annotation.SuppressLint
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AppDesignTokens defines the UI skeleton and typography hierarchy.
 * Using names like h1, body, and caption (instead of raw sizes)
 * ensures consistency across all screens, similar to Zoho/Salesforce apps.
 */
data class AppDesignTokens(
    val screenPadding: Dp,   // Outer margin of the screen
    val extraPadding: Dp,   // Outer margin of the screen
    val gridColumns: Int,    // Number of columns for dashboard cards
    val isTablet: Boolean,   // Used to switch between BottomBar and NavigationRail
    val cardHeight: Dp,      // Standard height for stat cards

    // --- Typography Scale ---
    val h1: TextUnit,        // Large titles (e.g., Lead Management header)
    val h2: TextUnit,        // Sub-headers or Names (e.g., Customer Name)
    val bodyLarge: TextUnit, // Primary data (e.g., Currency amounts, highlighted info)
    val bodyMedium: TextUnit,// Standard readable text (General content)
    val bodySmall: TextUnit, // Secondary info (e.g., Order IDs, Muted info)
    val caption: TextUnit,   // Micro-text (e.g., Breadcrumbs, Timestamps)
    val label: TextUnit,     // Tiny descriptors (e.g., Tags, Qty labels)

    // --- Component Scale (NEW) ---
    val fieldHeight: Dp,        // Height of text input fields (CusoTextField)
    val buttonHeight: Dp,       // Height of primary/secondary buttons
    val cardCornerRadius: Dp,   // Corner radius for Card/Box containers
    val cardPadding: Dp,        // Inner padding for auth cards (login/forgot/reset)
    val iconSize: Dp,           // Standard leading/trailing icon size in fields

    // --- OTP Input Scale (NEW) ---
    val otpBoxSize: Dp,         // Width/height of each OTP digit box
    val otpBoxSpacing: Dp       // Spacing between OTP digit boxes
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun getAdaptiveTokens(widthSize: WindowWidthSizeClass): AppDesignTokens {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    /**
     * FLUID TYPOGRAPHY LOGIC:
     * We calculate a fontScale based on a standard phone width (380dp).
     * This prevents text from looking too "chunky" on curved or wide phones.
     * The coerceIn(0.85f, 1.1f) keeps the scaling within a safe, readable range.
     */
    val fontScale = if (screenWidth < 600) (screenWidth / 380f).coerceIn(0.85f, 1.1f) else 1.0f

    return when (widthSize) {
        // COMPACT: Most standard smartphones (Moto G42, curved displays, etc.)
        WindowWidthSizeClass.Compact -> AppDesignTokens(
            extraPadding = 10.dp,
            screenPadding = 16.dp,
            gridColumns = 2,
            isTablet = false,
            cardHeight = 100.dp,
            h1 = (22 * fontScale).sp,
            h2 = (18 * fontScale).sp,
            bodyLarge = (16 * fontScale).sp,
            bodyMedium = (14 * fontScale).sp,
            bodySmall = (12 * fontScale).sp,
            caption = (11 * fontScale).sp,
            label = (10 * fontScale).sp,
            fieldHeight = 40.dp,
            buttonHeight = 44.dp,
            cardCornerRadius = 15.dp,
            cardPadding = 20.dp,
            iconSize = 18.dp,
            otpBoxSize = (44 * fontScale).dp,
            otpBoxSpacing = 8.dp
        )

        // MEDIUM: Foldable phones (Galaxy Fold) and small tablets (iPad Mini)
        WindowWidthSizeClass.Medium -> AppDesignTokens(
            extraPadding = 18.dp,

            screenPadding = 24.dp,
            gridColumns = 3,
            isTablet = true,
            cardHeight = 120.dp,
            h1 = 24.sp,
            h2 = 20.sp,
            bodyLarge = 18.sp,
            bodyMedium = 16.sp,
            bodySmall = 14.sp,
            caption = 12.sp,
            label = 11.sp,
            fieldHeight = 46.dp,
            buttonHeight = 50.dp,
            cardCornerRadius = 18.dp,
            cardPadding = 28.dp,
            iconSize = 20.dp,
            otpBoxSize = 52.dp,
            otpBoxSpacing = 10.dp
        )

        // EXPANDED: Large tablets (iPad Pro) and Desktop/Web views
        WindowWidthSizeClass.Expanded -> AppDesignTokens(
            extraPadding = 24.dp,

            screenPadding = 32.dp,
            gridColumns = 4,
            isTablet = true,
            cardHeight = 140.dp,
            h1 = 28.sp,
            h2 = 24.sp,
            bodyLarge = 22.sp,
            bodyMedium = 18.sp,
            bodySmall = 16.sp,
            caption = 14.sp,
            label = 12.sp,
            fieldHeight = 52.dp,
            buttonHeight = 56.dp,
            cardCornerRadius = 20.dp,
            cardPadding = 32.dp,
            iconSize = 22.dp,
            otpBoxSize = 58.dp,
            otpBoxSpacing = 12.dp
        )

        // Default fallback (uses Compact-like values)
        else -> AppDesignTokens(
            extraPadding = 10.dp,

            screenPadding = 16.dp,
            gridColumns = 2,
            isTablet = false,
            cardHeight = 100.dp,
            h1 = 22.sp,
            h2 = 18.sp,
            bodyLarge = 16.sp,
            bodyMedium = 14.sp,
            bodySmall = 12.sp,
            caption = 11.sp,
            label = 10.sp,
            fieldHeight = 40.dp,
            buttonHeight = 44.dp,
            cardCornerRadius = 15.dp,
            cardPadding = 20.dp,
            iconSize = 18.dp,
            otpBoxSize = 44.dp,
            otpBoxSpacing = 8.dp
        )
    }
}

/**
 * QUICK USAGE GUIDE:
 * 1. tokens.h1              -> Main Page Title
 * 2. tokens.h2               -> Customer Name / section header
 * 3. tokens.bodyLarge        -> Pricing/Total Amounts
 * 4. tokens.bodyMedium       -> Field placeholders, standard content
 * 5. tokens.bodySmall        -> Field typed text, Order ID, muted info
 * 6. tokens.caption          -> Breadcrumbs, helper text
 * 7. tokens.label            -> Tiny tags/errors
 * 8. tokens.fieldHeight      -> CusoTextField / BasicTextField height
 * 9. tokens.buttonHeight     -> Primary/secondary buttons
 * 10. tokens.cardCornerRadius -> Card / Box corner radius
 * 11. tokens.cardPadding     -> Inner padding of auth cards
 * 12. tokens.iconSize        -> Leading/trailing icons in fields
 * 13. tokens.otpBoxSize      -> Each OTP digit box size
 * 14. tokens.otpBoxSpacing   -> Spacing between OTP digit boxes
 */