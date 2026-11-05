package com.cuso.tailor.model.permission

data class PaywallModuleConfig(
    val moduleKey: String,
    val moduleTitle: String,             // Maps to "badge" (e.g., "Sales Module")
    val headline: String,                // Maps to "title" (e.g., "Upgrade Your Plan to Unlock Sales Tools")
    val description: String,             // Maps to "description"
    val efficiencyBadge: String,         // Maps to "growthText" (e.g., "+18% Revenue")
    val featuresIncluded: List<String>,  // Maps to "features"
    val heroTitle: String = "",          // Maps to "rightTitle" (e.g., "Increase Your Sales")
    val heroSubtitle: String = "",       // Maps to "rightDescription"
    val currentPlan: String = ""
)