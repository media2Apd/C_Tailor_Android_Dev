// com/cuso/mobile/utils/GovernmentIdValidator.kt
@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"

)
package com.cuso.mobile.view.home.reusablecomposables

import java.util.regex.Pattern

object GovernmentIdValidator {

    // ── PAN Card Validation ──
    // Format: ABCDE1234F (5 letters, 4 digits, 1 letter)
    // Pattern: [A-Z]{5}[0-9]{4}[A-Z]{1}
    private val PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$")

    fun validatePan(pan: String): ValidationResult {
        val cleaned = pan.trim().uppercase()

        if (cleaned.isEmpty()) {
            return ValidationResult(false, "PAN number is required")
        }

        if (cleaned.length != 10) {
            return ValidationResult(false, "PAN must be exactly 10 characters")
        }

        if (!PAN_PATTERN.matcher(cleaned).matches()) {
            return ValidationResult(false, "Invalid PAN format. Format: ABCDE1234F")
        }

        // PAN structure validation
        // First 5 characters: letters (ABCDE)
        // Next 4 characters: digits (1234)
        // Last character: letter (F)
        val firstFive = cleaned.substring(0, 5)
        val digits = cleaned.substring(5, 9)
        val lastChar = cleaned.substring(9)

        if (!firstFive.all { it.isLetter() }) {
            return ValidationResult(false, "First 5 characters must be letters")
        }

        if (!digits.all { it.isDigit() }) {
            return ValidationResult(false, "Characters 6-9 must be digits")
        }

        if (!lastChar[0].isLetter()) {
            return ValidationResult(false, "Last character must be a letter")
        }

        return ValidationResult(true, "Valid PAN number")
    }

    // ── Aadhaar Card Validation ──
    // Format: 12 digits
    // Last digit is checksum using Verhoeff algorithm
    private val AADHAAR_PATTERN = Pattern.compile("^[0-9]{12}$")

    fun validateAadhaar(aadhaar: String): ValidationResult {
        val cleaned = aadhaar.trim().replace(" ", "")

        if (cleaned.isEmpty()) {
            return ValidationResult(false, "Aadhaar number is required")
        }

        if (cleaned.length != 12) {
            return ValidationResult(false, "Aadhaar must be exactly 12 digits")
        }

        if (!AADHAAR_PATTERN.matcher(cleaned).matches()) {
            return ValidationResult(false, "Aadhaar must contain only digits")
        }

        // Check if all digits are same (invalid)
        if (cleaned.all { it == cleaned[0] }) {
            return ValidationResult(false, "Invalid Aadhaar number")
        }

        // Verhoeff checksum validation
        if (!isValidVerhoeff(cleaned)) {
            return ValidationResult(false, "Invalid Aadhaar number (checksum failed)")
        }

        return ValidationResult(true, "Valid Aadhaar number")
    }

    // ── Verhoeff Algorithm for Aadhaar ──
    // https://en.wikipedia.org/wiki/Verhoeff_algorithm
    private fun isValidVerhoeff(number: String): Boolean {
        val d = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
            intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
            intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
            intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
            intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
            intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
            intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
            intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
            intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        )

        val p = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
            intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
            intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
            intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
            intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
            intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
            intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
        )

        val inv = intArrayOf(0, 4, 3, 2, 1, 5, 6, 7, 8, 9)

        var c = 0
        val reversed = number.reversed()
        for (i in reversed.indices) {
            val digit = reversed[i].toString().toInt()
            c = d[c][p[i % 8][digit]]
        }

        return c == 0
    }

    // ── UAN (Universal Account Number) Validation ──
    // Format: 12 digits
    // Pattern: Usually starts with 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    // or 1000 to 9999
    private val UAN_PATTERN = Pattern.compile("^[0-9]{12}$")

    fun validateUan(uan: String): ValidationResult {
        val cleaned = uan.trim()

        if (cleaned.isEmpty()) {
            return ValidationResult(false, "UAN number is required")
        }

        if (cleaned.length != 12) {
            return ValidationResult(false, "UAN must be exactly 12 digits")
        }

        if (!UAN_PATTERN.matcher(cleaned).matches()) {
            return ValidationResult(false, "UAN must contain only digits")
        }

        // Check if all digits are same (invalid)
        if (cleaned.all { it == cleaned[0] }) {
            return ValidationResult(false, "Invalid UAN number")
        }

        // Check if starts with valid prefix
        val prefix = cleaned.substring(0, 2).toIntOrNull()
//        if (prefix != null && prefix !in 10..30) {
//            // Some UANs may start with other numbers, so this is a soft warning
//            // Not a hard error
//        }

        return ValidationResult(true, "Valid UAN number")
    }

    // ── Real-time validation state ──
    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )

    // ── Check if string contains only digits ──
    fun isNumeric(str: String): Boolean {
        return str.all { it.isDigit() }
    }
}