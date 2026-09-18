package com.cuso.tailor.network

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Global Gson Adapter Factory that converts null JSON arrays into emptyList()
 * across all API responses to prevent Kotlin NullPointerExceptions.
 */
class SafeListTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? {
        if (!List::class.java.isAssignableFrom(typeToken.rawType)) {
            return null
        }

        val delegateAdapter: TypeAdapter<T> = gson.getDelegateAdapter(this, typeToken)

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                delegateAdapter.write(out, value)
            }

            @Suppress("UNCHECKED_CAST")
            override fun read(reader: JsonReader): T {
                return if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    emptyList<Any>() as T
                } else {
                    try {
                        delegateAdapter.read(reader) ?: (emptyList<Any>() as T)
                    } catch (_: Exception) {
                        reader.skipValue()
                        emptyList<Any>() as T
                    }
                }
            }
        }
    }
}