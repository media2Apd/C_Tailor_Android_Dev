package com.cuso.mobile.utils

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

fun isValidPhoneNumber(phone: String, isoCode: String): Boolean {
    return try {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val numberProto = phoneUtil.parse(phone, isoCode)
        phoneUtil.isValidNumber(numberProto)
    } catch (_: NumberParseException) {
        false
    }
}