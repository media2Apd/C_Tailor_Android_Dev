@file:Suppress("UNUSED_PARAMETER", "unused","AssignedValueIsNeverRead","unusedVariable","VariableNeverRead")

package com.cuso.tailor.utils

import android.os.Build

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cuso.tailor.database.dao.SettingsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency as JavaCurrency
import java.util.Locale

val LocalCurrencySymbol = compositionLocalOf { "" }

object CurrencyHelper {

    private const val TAG = "CurrencyHelperDebugLogs"

    var currentCode: String? by mutableStateOf(null)
        private set

    var currentSymbol: String by mutableStateOf("")
        private set

    private val _symbolFlow = MutableStateFlow("")
    val symbolFlow: StateFlow<String> = _symbolFlow.asStateFlow()

    fun startObserving(settingsDao: SettingsDao, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val cachedSettings = settingsDao.getSettings()
                val code = normalizeCode(cachedSettings?.currency)
                currentCode = code
                val resolvedSymbol = resolveSymbolForCode(code)

                currentSymbol = resolvedSymbol
                _symbolFlow.value = resolvedSymbol

            } catch (e: Exception) {
            }
        }

        settingsDao.getSettingsFlow()
            .map { normalizeCode(it?.currency) }
            .distinctUntilChanged()
            .onEach { code ->
                currentCode = code
                val resolvedSymbol = resolveSymbolForCode(code)

                currentSymbol = resolvedSymbol
                _symbolFlow.value = resolvedSymbol

            }
            .launchIn(scope)
    }

    /**
     * Resolves the currency symbol.
     * Prioritizes Room DB's active currency over legacy "INR" fallbacks.
     */
    fun getSymbol(currencyCode: String? = null): String {
        // Settings currency from Room DB always wins
        val dbCode = normalizeCode(currentCode)
        // Passed code is used only when the database has no currency yet
        val passedCode = normalizeCode(currencyCode)

        val targetCode = dbCode ?: passedCode ?: return ""

        return resolveSymbolForCode(targetCode)
    }

    private fun localeForCurrency(code: String): Locale {
        return when (code) {
            "INR" -> Locale("en", "IN")
            else -> Locale.getDefault()
        }
    }

    /**
     * Formats an amount with the active currency.
     * - amount == null: returns only the currency symbol (same as the JS helper)
     * - autoDecimals = true: whole numbers show no decimals, others show [decimals] digits
     */
    fun format(
        amount: Number?,
        currencyCode: String? = null,
        decimals: Int = 2,
        autoDecimals: Boolean = true
    ): String {
        val code = normalizeCode(currentCode) ?: normalizeCode(currencyCode)
        ?: return amount?.toString() ?: ""

        if (amount == null) return resolveSymbolForCode(code)

        val value = amount.toDouble()
        val isWhole = value == Math.floor(value) && !value.isInfinite()
        val fraction = if (autoDecimals && isWhole) 0 else decimals

        return try {
            val formatter = NumberFormat.getCurrencyInstance(localeForCurrency(code)) as DecimalFormat
            formatter.currency = JavaCurrency.getInstance(code)

            // Use the same symbol that getSymbol() returns so UI stays consistent
            val symbols = formatter.decimalFormatSymbols
            symbols.currencySymbol = resolveSymbolForCode(code)
            formatter.decimalFormatSymbols = symbols

            formatter.minimumFractionDigits = fraction
            formatter.maximumFractionDigits = decimals
            formatter.format(value)
        } catch (e: Exception) {
            "${resolveSymbolForCode(code)}$value"
        }
    }

    private fun resolveSymbolForCode(code: String?): String {
        if (code.isNullOrBlank()) return ""

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val icuCurrency = android.icu.util.Currency.getInstance(code)
                icuCurrency.getName(
                    Locale.getDefault(),
                    android.icu.util.Currency.NARROW_SYMBOL_NAME,
                    null
                ) ?: icuCurrency.getSymbol(Locale.getDefault()) ?: icuCurrency.symbol ?: ""
            } else {
                JavaCurrency.getInstance(code).getSymbol(Locale.getDefault())
            }
        } catch (e: Exception) {
            ""
        }
    }
}
/**
 * Cleans a raw currency value from the database.
 * Accepts values like "INR", " eur ", "CNY (CY)" and returns a valid 3 letter ISO code or null.
 */
private fun normalizeCode(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val token = Regex("[A-Za-z]{3}").find(raw)?.value?.uppercase() ?: return null
    return try {
        JavaCurrency.getInstance(token)
        token
    } catch (e: Exception) {
        null
    }
}


@Composable
fun currentCurrency(): String = LocalCurrencySymbol.current

fun currencySymbol(code: String? = null): String = CurrencyHelper.getSymbol(code)

fun String?.toformatCurrency(): String = CurrencyHelper.getSymbol(this)

fun formatCurrency(
    amount: Number? = null,
    currency: String? = null,
    decimals: Int = 2,
    autoDecimals: Boolean = true
): String = CurrencyHelper.format(amount, currency, decimals, autoDecimals)