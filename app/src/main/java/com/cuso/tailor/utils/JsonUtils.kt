package com.cuso.tailor.utils

import android.content.Context
import com.cuso.tailor.model.sales.JsonCountry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

suspend fun loadJsonFromAssets(context: Context, fileName: String): List<JsonCountry>? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<JsonCountry>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}