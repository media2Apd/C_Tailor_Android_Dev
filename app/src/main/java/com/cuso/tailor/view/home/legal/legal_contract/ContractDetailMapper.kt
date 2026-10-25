@file:Suppress("unused", "DEPRECATION")

package com.cuso.tailor.view.home.legal.legal_contract

import com.cuso.tailor.model.legal.LegalActivityDto
import com.cuso.tailor.model.legal.LegalContractDto
import com.cuso.tailor.model.legal.LegalDocumentDto
import com.cuso.tailor.view.home.legal.detail_template.DetailActivityItem
import com.cuso.tailor.view.home.legal.detail_template.DetailStepItem
import com.cuso.tailor.view.home.legal.detail_template.TimelineState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────
// UI models used by the overview screen
// ─────────────────────────────────────────────────────────────

data class ContractPartyUi(
    val heading: String,
    val tag: String,
    val isPrimary: Boolean,
    val name: String,
    val type: String,
    val country: String,
    val role: String
)

data class ContractDocumentUi(
    val name: String,
    val url: String,
    val category: String,
    val version: Int,
    val extension: String
)

data class RenewalActionConfig(
    val status: String,
    val label: String,
    val isSecondary: Boolean = false,
    val isApproval: Boolean = false
)

data class NextStepInfo(
    val text: String,
    val isStartRenewal: Boolean = false,
    val actions: List<RenewalActionConfig> = emptyList()
)

data class ContractDetailUi(
    val title: String,
    val code: String,
    val type: String,
    val country: String,
    val region: String,
    val owner: String,
    val statusLabel: String,
    val riskLabel: String,
    val timeLeft: String,
    val expiry: String,
    val startDate: String,
    val partyName: String,
    val partyType: String,
    val daysRemaining: String,
    val renewalStatus: String,
    val noticePeriod: String,
    val renewalCycle: String,
    val renewalOwner: String,
    val nextAction: String,
    val nextActionTag: String,
    val bannerMessage: String?,
    val parties: List<ContractPartyUi>,
    val terms: List<Pair<String, String>>,
    val documents: List<ContractDocumentUi>,
    val steps: List<DetailStepItem>,
    val activities: List<DetailActivityItem>,
    val nextStepInfo: NextStepInfo
) {
    val location: String
        get() = listOf(country, region).filter { it.isNotBlank() && it != "-" }.joinToString(" / ").ifBlank { "-" }
}

// ─────────────────────────────────────────────────────────────
// Date helpers
// ─────────────────────────────────────────────────────────────

private const val DAY_MS = 24L * 60 * 60 * 1000
private const val API_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val EMPTY = "-"

private fun parseIso(raw: String?): Date? {
    if (raw.isNullOrBlank()) return null
    return try {
        SimpleDateFormat(API_DATE_PATTERN, Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(raw)
    } catch (_: Exception) {
        null
    }
}

private fun formatDateOnly(raw: String?): String {
    val date = parseIso(raw) ?: return EMPTY
    return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(date)
}

private fun formatLocalDate(raw: String?): String {
    val date = parseIso(raw) ?: return EMPTY
    return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(date)
}

private fun formatLocalTime(raw: String?): String {
    val date = parseIso(raw) ?: return EMPTY
    return SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(date)
}

private fun daysUntil(raw: String?): Int? {
    val end = parseIso(raw) ?: return null
    val diff = end.time - System.currentTimeMillis()
    return (-Math.floorDiv(-diff, DAY_MS)).toInt()
}

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: EMPTY

private fun String.humanize(): String = replace("_", " ").trim()

// ─────────────────────────────────────────────────────────────
// State Transition Logic matching updateRenewalStatusService
// ─────────────────────────────────────────────────────────────

fun getNextStep(status: String, hasProposal: Boolean): NextStepInfo {
    if (!hasProposal && status == "Renewal_Notice") {
        return NextStepInfo(
            text = "Propose new dates and terms to start the renewal",
            isStartRenewal = true
        )
    }
    return when (status) {
        "Renewal_Notice" -> NextStepInfo(
            text = "Review renewal terms",
            actions = listOf(RenewalActionConfig(status = "In_Review", label = "Start Review"))
        )
        "In_Review" -> NextStepInfo(
            text = "Submit the renewal for approval",
            actions = listOf(RenewalActionConfig(status = "Pending_Approval", label = "Submit for Approval"))
        )
        "Pending_Approval" -> NextStepInfo(
            text = "Approve or reject the renewal",
            actions = listOf(
                RenewalActionConfig(status = "Approved", label = "Approve", isApproval = true),
                RenewalActionConfig(status = "Rejected", label = "Reject", isSecondary = true, isApproval = true)
            )
        )
        "Rejected" -> NextStepInfo(
            text = "Revise the terms and send the renewal back to review",
            actions = listOf(RenewalActionConfig(status = "In_Review", label = "Move to Review"))
        )
        "Approved" -> NextStepInfo(
            text = "Complete the renewal",
            actions = listOf(RenewalActionConfig(status = "Renewed", label = "Mark Complete"))
        )
        "Renewed" -> NextStepInfo(
            text = "Renewal completed",
            actions = emptyList()
        )
        else -> NextStepInfo(text = "—", actions = emptyList())
    }
}

fun formatRenewalLabel(status: String?): String {
    return when (status) {
        "Renewal_Notice" -> "Renewal Notice"
        "In_Review" -> "In Review"
        "Pending_Approval" -> "Pending Approval"
        "Approved" -> "Approved"
        "Rejected" -> "Rejected"
        "Renewed" -> "Renewed"
        else -> status ?: "Pending"
    }
}

// ─────────────────────────────────────────────────────────────
// Mapper Implementation
// ─────────────────────────────────────────────────────────────

fun LegalContractDto.toContractDetailUi(): ContractDetailUi {
    val daysLeft = daysUntil(endDate)
    val statusLabel = resolveStatus(daysLeft, renewalNoticePeriod)
    val ownerName = internalBusinessOwner?.fullName?.takeIf { it.isNotBlank() }
        ?: legalOwner?.fullName?.takeIf { it.isNotBlank() }
        ?: EMPTY
    val renewalOwnerName = renewalOwner?.fullName?.takeIf { it.isNotBlank() } ?: ownerName
    val expiryText = formatDateOnly(endDate)

    val currentRenewalStatus = renewalStatus ?: "Renewal_Notice"
    val hasProposal = !proposedStartDate.isNullOrBlank()
    val nextStepInfo = getNextStep(currentRenewalStatus, hasProposal)

    return ContractDetailUi(
        title = contractName.orDash(),
        code = contractNumber.orDash(),
        type = contractType.orDash(),
        country = countryName.orDash(),
        region = region.orDash(),
        owner = ownerName,
        statusLabel = statusLabel,
        riskLabel = riskLevel.orDash(),
        timeLeft = buildTimeLeft(daysLeft),
        expiry = expiryText,
        startDate = formatDateOnly(startDate),
        partyName = primaryParty.orDash(),
        partyType = partyType.orDash(),
        daysRemaining = daysLeft?.let { if (it < 0) "Expired" else "$it Days" } ?: EMPTY,
        renewalStatus = formatRenewalLabel(currentRenewalStatus),
        noticePeriod = "$renewalNoticePeriod Days",
        renewalCycle = "Renewal #$renewalNumber",
        renewalOwner = renewalOwnerName,
        nextAction = nextStepInfo.text,
        nextActionTag = priority.orDash(),
        bannerMessage = buildBanner(statusLabel, daysLeft, expiryText),
        parties = buildParties(),
        terms = buildTerms(),
        documents = buildDocuments(),
        steps = buildSteps(),
        activities = buildActivities(),
        nextStepInfo = nextStepInfo
    )
}

private fun resolveStatus(daysLeft: Int?, noticeDays: Int): String = when {
    daysLeft == null -> "Active"
    daysLeft < 0 -> "Expired"
    daysLeft <= noticeDays.coerceAtLeast(30) -> "Expiring Soon"
    else -> "Active"
}

private fun buildTimeLeft(daysLeft: Int?): String = when {
    daysLeft == null -> EMPTY
    daysLeft < 0 -> "Expired ${abs(daysLeft)} Days Ago"
    daysLeft == 0 -> "Expires Today"
    else -> "$daysLeft Days Remaining"
}

private fun buildBanner(statusLabel: String, daysLeft: Int?, expiryText: String): String? = when (statusLabel) {
    "Expired" -> "This agreement expired on $expiryText. Review the contract terms and initiate renewal."
    "Expiring Soon" -> "This agreement expires in ${daysLeft ?: 0} days. Review the contract terms and initiate renewal before $expiryText."
    else -> null
}

private fun LegalContractDto.buildParties(): List<ContractPartyUi> = listOf(
    ContractPartyUi(
        heading = "PRIMARY PARTY",
        tag = "ORIGINATOR",
        isPrimary = true,
        name = primaryParty.orDash(),
        type = partyType.orDash(),
        country = countryName.orDash(),
        role = partyType.orDash()
    ),
    ContractPartyUi(
        heading = "SECONDARY PARTY",
        tag = "INTERNAL LEGAL ENTITY",
        isPrimary = false,
        name = legalEntity.orDash(),
        type = "Business Entity",
        country = countryName.orDash(),
        role = "Internal Legal Entity"
    )
)

private fun LegalContractDto.buildTerms(): List<Pair<String, String>> {
    val valueText = NumberFormat.getInstance(Locale("en", "IN")).format(contractValue)
    return buildList {
        add("Payment Terms" to (paymentTerms?.name).orDash())
        add("Renewal Type" to renewalType.orDash())
        add("Notice Period" to "$renewalNoticePeriod days")
        add("Contract Value" to "${currency.orEmpty()} $valueText".trim())
        add("Category" to contractCategory.orDash())
        add("Priority" to priority.orDash())
        add("Confidentiality" to confidentiality.orDash())
        add("Legal Entity" to legalEntity.orDash())
        keyTermsAndConditions?.takeIf { it.isNotBlank() }?.let { add("Key Terms" to it) }
        contractDescription?.takeIf { it.isNotBlank() }?.let { add("Description" to it) }
    }
}

private fun LegalContractDto.buildDocuments(): List<ContractDocumentUi> =
    mainDocuments.orEmpty().map { it.toUi("Main document") } +
            supportingDocuments.orEmpty().map { it.toUi("Supporting document") }

private fun LegalDocumentDto.toUi(category: String): ContractDocumentUi {
    val name = documentName.orDash()
    return ContractDocumentUi(
        name = name,
        url = documentUrl.orEmpty(),
        category = category,
        version = version,
        extension = name.substringAfterLast('.', "")
    )
}

private fun LegalContractDto.buildSteps(): List<DetailStepItem> {
    val status = renewalStatus ?: "Renewal_Notice"
    val reachedReview = listOf("In_Review", "Pending_Approval", "Approved", "Rejected", "Renewed").contains(status)
    val reachedApproval = listOf("Pending_Approval", "Approved", "Rejected", "Renewed").contains(status)

    val rawSteps = listOf(
        DetailStepItem(
            title = "Contract Created",
            subtitle = "Created on ${formatLocalDate(createdAt)}",
            state = TimelineState.DONE
        ),
        DetailStepItem(
            title = "Active",
            subtitle = "In effect since ${formatDateOnly(startDate)}",
            state = if (!startDate.isNullOrBlank()) TimelineState.DONE else TimelineState.PENDING
        ),
        DetailStepItem(
            title = "Renewal Notice",
            subtitle = if (!proposedStartDate.isNullOrBlank()) "Proposal submitted" else "Notice period: $renewalNoticePeriod days",
            state = if (!proposedStartDate.isNullOrBlank()) TimelineState.DONE else TimelineState.CURRENT
        ),
        DetailStepItem(
            title = "Review",
            subtitle = if (reachedReview) "Terms under review" else "Pending review",
            state = if (reachedReview) TimelineState.DONE else TimelineState.PENDING
        ),
        DetailStepItem(
            title = if (status == "Rejected") "Rejected" else "Approval",
            subtitle = when {
                status == "Rejected" -> "Rejected on ${formatLocalDate(rejectedAt)}"
                approvedAt != null -> "Approved on ${formatLocalDate(approvedAt)}"
                submittedAt != null -> "Submitted on ${formatLocalDate(submittedAt)}"
                else -> "Pending approval"
            },
            state = if (reachedApproval) TimelineState.DONE else TimelineState.PENDING
        ),
        DetailStepItem(
            title = "Renewed",
            subtitle = if (completedAt != null) "Completed on ${formatLocalDate(completedAt)}" else "Target: ${formatDateOnly(endDate)}",
            state = if (status == "Renewed") TimelineState.DONE else TimelineState.PENDING
        )
    )

    return rawSteps
}

private fun LegalContractDto.buildActivities(): List<DetailActivityItem> =
    activity.orEmpty()
        .sortedByDescending { parseIso(it.performedAt)?.time ?: 0L }
        .map { it.toUi() }

private fun LegalActivityDto.toUi(): DetailActivityItem = DetailActivityItem(
    date = formatLocalDate(performedAt),
    time = formatLocalTime(performedAt),
    title = action?.humanize().orDash(),
    subtitle = description?.takeIf { it.isNotBlank() }
        ?: performedBy?.fullName.orDash()
)