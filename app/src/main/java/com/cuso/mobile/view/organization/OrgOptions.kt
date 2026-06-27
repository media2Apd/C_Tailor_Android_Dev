package com.cuso.mobile.view.organization

object OrgOptions {
    val orgTypes = listOf(
        "Sole Proprietorship", "Partnership Firm",
        "Limited Liability Partnership (LLP)", "Private Limited Company (Pvt Ltd)",
        "Public Limited Company", "One Person Company (OPC)",
        "Section 8 Company / NGO", "Trust / Society",
        "Civil / Local Body", "Other"
    )

    val businessTypes = listOf(
        "Bespoke Tailoring", "Alternations & Repairs",
        "Apparel Retailer", "Uniform & Corporate Wear", "Other"
    )

    val companySizes = listOf(
        "1-10 employees", "11-50 employees", "51-200 employees",
        "201-500 employees", "500+ employees"
    )

    val segments = listOf("B2B", "B2C", "Both")

    val currencies: List<String> by lazy {
        java.util.Currency.getAvailableCurrencies()
            .sortedBy { it.currencyCode }
            .map { "${it.currencyCode} " }
    }

    val languages: List<String> by lazy {
        java.util.Locale.getAvailableLocales()
            .map { it.displayLanguage }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val timezones: List<String> by lazy {
        java.util.TimeZone.getAvailableIDs().map { id ->
            val tz = java.util.TimeZone.getTimeZone(id)
            val offsetHours = tz.rawOffset / 3600000
            val sign = if (offsetHours >= 0) "+" else ""
            "$id (UTC$sign$offsetHours)"
        }.sorted()
    }
}