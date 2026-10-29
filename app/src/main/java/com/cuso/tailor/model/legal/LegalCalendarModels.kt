package com.cuso.tailor.model.legal

import com.google.gson.annotations.SerializedName

data class LegalCalendarResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: LegalCalendarData? = null,
    @SerializedName("message") val message: String? = null
)

data class LegalCalendarData(
    @SerializedName("range") val range: CalendarDateRange? = null,
    @SerializedName("events") val events: List<LegalCalendarEvent> = emptyList(),
    @SerializedName("summary") val summary: CalendarSummary? = null,
    @SerializedName("urgent") val urgent: List<LegalCalendarEvent> = emptyList()
)

data class CalendarDateRange(
    @SerializedName("from") val from: String? = null,
    @SerializedName("to") val to: String? = null
)

data class LegalCalendarEvent(
    @SerializedName("id") val id: String = "",
    @SerializedName("module") val module: String? = null,
    @SerializedName("moduleLabel") val moduleLabel: String? = null,
    @SerializedName("recordId") val recordId: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("dateField") val dateField: String? = null,
    @SerializedName("eventType") val eventType: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("closed") val closed: Boolean = false,
    @SerializedName("riskLevel") val riskLevel: String? = null
)

data class CalendarSummary(
    @SerializedName("today") val today: Int = 0,
    @SerializedName("thisWeek") val thisWeek: Int = 0,
    @SerializedName("next30Days") val next30Days: Int = 0,
    @SerializedName("overdue") val overdue: Int = 0,
    @SerializedName("upcoming") val upcoming: Int = 0
)