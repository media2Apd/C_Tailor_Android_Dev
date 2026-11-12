package com.cuso.tailor.utils

import android.os.Build
import android.util.Log
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
                val code = cachedSettings?.currency?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
                currentCode = code
                val resolvedSymbol = resolveSymbolForCode(code)

                currentSymbol = resolvedSymbol
                _symbolFlow.value = resolvedSymbol

                Log.d(TAG, "startObserving() initial code: '$code', symbol: '$resolvedSymbol'")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching cached settings: ${e.message}", e)
            }
        }

        settingsDao.getSettingsFlow()
            .map { it?.currency?.trim()?.uppercase()?.takeIf { code -> code.isNotBlank() } }
            .distinctUntilChanged()
            .onEach { code ->
                currentCode = code
                val resolvedSymbol = resolveSymbolForCode(code)

                currentSymbol = resolvedSymbol
                _symbolFlow.value = resolvedSymbol

                Log.d(TAG, "startObserving() live update code: '$code', symbol: '$resolvedSymbol'")
            }
            .launchIn(scope)
    }

    /**
     * Resolves the currency symbol.
     * Prioritizes Room DB's active currency over legacy "INR" fallbacks.
     */
    fun getSymbol(currencyCode: String? = null): String {
        val dbCode = currentCode?.trim()?.uppercase()?.takeIf { it.isNotBlank() }
        val passedCode = currencyCode?.trim()?.uppercase()?.takeIf { it.isNotBlank() }

        // If Room DB has a code (e.g. CNY), use it even if caller passes legacy fallback "INR"
        val targetCode = when {
            // Case 1: Caller passed an explicit non-INR code different from default
            passedCode != null && passedCode != "INR" -> passedCode
            // Case 2: Room DB code exists (e.g., CNY)
            dbCode != null -> dbCode
            // Case 3: Fallback to passed code if Room DB is empty
            passedCode != null -> passedCode
            // Case 4: No currency available anywhere -> return empty string ""
            else -> return ""
        }

        return resolveSymbolForCode(targetCode)
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
            Log.e(TAG, "Exception getting symbol for code '$code': ${e.message}")
            ""
        }
    }
}

@Composable
fun currentCurrency(): String = LocalCurrencySymbol.current

fun currencySymbol(code: String? = null): String = CurrencyHelper.getSymbol(code)

fun String?.toCurrencySymbol(): String = CurrencyHelper.getSymbol(this)