package com.cuso.tailor.model.legal

import com.google.gson.annotations.SerializedName

data class LegalDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LegalDashboardData?
)

data class LegalDashboardData(
    @SerializedName("generatedAt") val generatedAt: String?,
    @SerializedName("year") val year: Int?,
    @SerializedName("kpis") val kpis: LegalKpis?,
    @SerializedName("trend") val trend: List<LegalTrendItem>?,
    @SerializedName("health") val health: List<LegalHealthItem>?,
    @SerializedName("riskDistribution") val riskDistribution: LegalRiskDistribution?,
    @SerializedName("categories") val categories: List<LegalCategoryItem>?,
    @SerializedName("upcoming") val upcoming: List<LegalUpcomingItem>?,
    @SerializedName("approvals") val approvals: List<LegalApprovalItem>?
)

data class LegalKpis(
    @SerializedName("totalRecords") val totalRecords: KpiMetric?,
    @SerializedName("criticalRisks") val criticalRisks: KpiMetric?,
    @SerializedName("expiringContracts") val expiringContracts: KpiMetric?,
    @SerializedName("overdueActions") val overdueActions: KpiMetric?,
    @SerializedName("pendingApprovals") val pendingApprovals: KpiMetric?,
    @SerializedName("complianceScore") val complianceScore: KpiMetric?,
    @SerializedName("newCases") val newCases: KpiMetric?
)

data class KpiMetric(
    @SerializedName("value") val value: Double?,
    @SerializedName("change") val change: Double?,
    @SerializedName("changeType") val changeType: String?,
    @SerializedName("series") val series: List<Double?>?
)

data class LegalTrendItem(
    @SerializedName("month") val month: String?,
    @SerializedName("compliance") val compliance: Double?,
    @SerializedName("openRisk") val openRisk: Double?
)

data class LegalHealthItem(
    @SerializedName("key") val key: String?,
    @SerializedName("label") val label: String?,
    @SerializedName("percent") val percent: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("open") val open: Int?
)

data class LegalRiskDistribution(
    @SerializedName("total") val total: Int?,
    @SerializedName("items") val items: List<RiskDistributionItem>?
)

data class RiskDistributionItem(
    @SerializedName("key") val key: String?,
    @SerializedName("label") val label: String?,
    @SerializedName("count") val count: Int?,
    @SerializedName("percent") val percent: Double?
)

data class LegalCategoryItem(
    @SerializedName("key") val key: String?,
    @SerializedName("label") val label: String?,
    @SerializedName("records") val records: Int?,
    @SerializedName("open") val open: Int?,
    @SerializedName("highRisk") val highRisk: Int?
)

data class LegalUpcomingItem(
    @SerializedName("id") val id: String?,
    @SerializedName("module") val module: String?,
    @SerializedName("moduleLabel") val moduleLabel: String?,
    @SerializedName("recordId") val recordId: String?,
    @SerializedName("number") val number: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("dateField") val dateField: String?,
    @SerializedName("eventType") val eventType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("closed") val closed: Boolean?,
    @SerializedName("riskLevel") val riskLevel: String?,
    @SerializedName("owner") val owner: String?
)

data class LegalApprovalItem(
    @SerializedName("id") val id: String?,
    @SerializedName("module") val module: String?,
    @SerializedName("moduleLabel") val moduleLabel: String?,
    @SerializedName("recordId") val recordId: String?,
    @SerializedName("subId") val subId: String?,
    @SerializedName("requestNumber") val requestNumber: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("recordName") val recordName: String?,
    @SerializedName("requestedBy") val requestedBy: String?,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("stage") val stage: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("decidedAt") val decidedAt: String?,
    @SerializedName("decidedBy") val decidedBy: String?,
    @SerializedName("decisionComment") val decisionComment: String?
)