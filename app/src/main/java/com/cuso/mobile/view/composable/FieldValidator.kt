package com.cuso.mobile.view.composable

data class ValidationField(val key: String, val value: String, val errorMessage: String)
data class ValidationResult(val fieldKey: String, val message: String)

object FieldValidator {
    fun validate(fields: List<ValidationField>): ValidationResult? {
        val failed = fields.firstOrNull { it.value.isBlank() } ?: return null
        return ValidationResult(fieldKey = failed.key, message = failed.errorMessage)
    }

    fun resolveSection(fieldKey: String?, sectionFieldMap: Map<String, List<String>>): String? {
        if (fieldKey == null) return null
        return sectionFieldMap.entries.firstOrNull { (_, fields) -> fieldKey in fields }?.key
    }
}