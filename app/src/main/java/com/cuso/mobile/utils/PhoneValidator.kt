package com.cuso.mobile.utils

import com.google.i18n.phonenumbers.PhoneNumberUtil

private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
//
////   NO hardcoded map — library gives accurate example number length per country
//fun maxDigitsFor(iso: String): Int {
//    return try {
//        val example = phoneUtil.getExampleNumber(iso.uppercase())
//        example?.nationalNumber?.toString()?.length ?: 15
//    } catch (e: Exception) {
//        15   // safe fallback if ISO code unsupported
//    }
//}

//   Optional — real validation (not just length check)
fun isValidPhoneNumber(digits: String, iso: String): Boolean {
    return try {
        val parsed = phoneUtil.parse(digits, iso.uppercase())
        phoneUtil.isValidNumber(parsed)
    } catch (_: Exception) {
        false
    }
}

//   Visual "-" formatting after 5th digit — unrelated to library, kept as-is
//class PhoneNumberVisualTransformation : VisualTransformation {
//    override fun filter(text: AnnotatedString): TransformedText {
//        val digits = text.text
//        val formatted = if (digits.length <= 5) {
//            digits
//        } else {
//            "${digits.substring(0, 5)}-${digits.substring(5)}"
//        }
//
//        val offsetMapping = object : OffsetMapping {
//            override fun originalToTransformed(offset: Int): Int {
//                return if (offset <= 5) offset else offset + 1
//            }
//
//            override fun transformedToOriginal(offset: Int): Int {
//                return if (offset <= 5) offset else (offset - 1).coerceAtMost(digits.length)
//            }
//        }
//
//        return TransformedText(AnnotatedString(formatted), offsetMapping)
//    }
//}