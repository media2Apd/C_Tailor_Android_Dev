package com.cuso.tailor.model.service

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

private fun JsonObject.str(key: String): String? =
    get(key)?.takeIf { it.isJsonPrimitive }?.asString

/** stageId: "id-string"  OR  { "_id": "...", "name": "..." } */
class StageMetaRefAdapter : JsonDeserializer<ServiceStageMetaRef?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ServiceStageMetaRef? = when {
        json.isJsonNull -> null
        json.isJsonPrimitive -> ServiceStageMetaRef(id = json.asString)
        json.isJsonObject -> json.asJsonObject.let {
            ServiceStageMetaRef(
                id = it.str("_id"),
                name = it.str("name"),
                code = it.str("code"),
                workType = it.str("workType")
            )
        }
        else -> null
    }
}

/** workerId: "id-string"  OR  { "_id": "...", "firstName": "..." } */
class WorkerRefAdapter : JsonDeserializer<ServiceWorkerRef?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ServiceWorkerRef? = when {
        json.isJsonNull -> null
        json.isJsonPrimitive -> ServiceWorkerRef(id = json.asString)
        json.isJsonObject -> json.asJsonObject.let {
            ServiceWorkerRef(
                id = it.str("_id") ?: "",
                firstName = it.str("firstName"),
                lastName = it.str("lastName"),
                employeeCode = it.str("employeeCode"),
                memberId = it.str("memberId")
            )
        }
        else -> null
    }
}