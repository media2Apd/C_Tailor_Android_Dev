package com.cuso.mobile.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object SafeAnyListDeserializer : JsonDeserializer<List<Any>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Any> {
        if (json == null || json.isJsonNull) return emptyList()
        if (!json.isJsonArray) return emptyList()

        val result = mutableListOf<Any>()
        for (element in json.asJsonArray) {
            try {
                when {
                    element.isJsonNull -> { /* skip */ }
                    element.isJsonObject -> {
                        val map = mutableMapOf<String, Any?>()
                        for ((key, value) in element.asJsonObject.entrySet()) {
                            map[key] = jsonElementToPlain(value)
                        }
                        result.add(map)
                    }
                    element.isJsonPrimitive -> {
                        val prim = element.asJsonPrimitive
                        when {
                            prim.isString -> result.add(prim.asString)
                            prim.isNumber -> result.add(prim.asString)
                            prim.isBoolean -> result.add(prim.asBoolean)
                        }
                    }
                    else -> { /* skip */ }
                }
            } catch (_: Exception) {
                // skip bad element only, don't crash whole list
            }
        }
        return result
    }

    private fun jsonElementToPlain(element: JsonElement): Any? {
        return when {
            element.isJsonNull -> null
            element.isJsonObject -> element.asJsonObject.entrySet()
                .associate { (k, v) -> k to jsonElementToPlain(v) }
            element.isJsonArray -> element.asJsonArray.map { jsonElementToPlain(it) }
            element.isJsonPrimitive -> {
                val p = element.asJsonPrimitive
                when {
                    p.isString -> p.asString
                    p.isBoolean -> p.asBoolean
                    p.isNumber -> p.asString
                    else -> null
                }
            }
            else -> null
        }
    }
}