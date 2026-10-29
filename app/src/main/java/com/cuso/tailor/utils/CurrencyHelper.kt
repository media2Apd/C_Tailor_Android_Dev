package com.cuso.tailor.utils

import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cuso.tailor.database.dao.SettingsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.Currency
import java.util.Locale

object CurrencyHelper {

    // Holds the current currency code (e.g. "EUR") for the whole app.
    // It is a Compose state, so any Text using currencySymbol() recomposes when it changes.
    var currentCode: String? by mutableStateOf(null)
        private set

    // Call once from Application.onCreate().
    // Keeps currentCode in sync with the settings table automatically.
    fun startObserving(settingsDao: SettingsDao, scope: CoroutineScope) {
        settingsDao.getSettingsFlow()
            .map { it?.currency }
            .distinctUntilChanged()
            .onEach { currentCode = it }
            .launchIn(scope)
    }

    // Converts an ISO-4217 code (e.g. "EUR") to its symbol (e.g. "€")
    fun getSymbol(currencyCode: String?): String {
        if (currencyCode.isNullOrBlank()) return ""

        val code = currencyCode.trim().uppercase()

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android ICU gives the clean narrow symbol (e.g. $, ₹, €)
                val icuCurrency = android.icu.util.Currency.getInstance(code)
                icuCurrency.getName(
                    Locale.getDefault(),
                    android.icu.util.Currency.NARROW_SYMBOL_NAME,
                    null
                ) ?: icuCurrency.symbol ?: code
            } else {
                // Fallback for older Android versions
                Currency.getInstance(code).getSymbol(Locale.getDefault())
            }
        } catch (e: Exception) {
            // Unknown currency, return the raw code
            currencyCode
        }
    }
}

// Extension helper (still works if you want to pass a code manually)
fun String?.toCurrencySymbol(): String = CurrencyHelper.getSymbol(this)

// THE ONE FUNCTION: just call currencySymbol() anywhere, no variable needed.
// It reads the currency saved in settings (EUR -> €, INR -> ₹, USD -> $).
fun currencySymbol(code: String? = null): String {
    return CurrencyHelper.getSymbol(code ?: CurrencyHelper.currentCode)
}