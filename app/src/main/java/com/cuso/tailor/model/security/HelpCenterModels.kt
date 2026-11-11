package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

data class HelpArticleListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: HelpArticleListData? = null,
    @SerializedName("message") val message: String? = null
)

data class HelpArticleListData(
    @SerializedName("articles") val articles: List<HelpArticleItem> = emptyList(),
    @SerializedName("meta") val meta: HelpArticleMeta? = null
)

data class HelpArticleMeta(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 4,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class HelpArticleItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("author") val author: String? = null,
    @SerializedName("sections") val sections: List<HelpArticleSection> = emptyList(),
    @SerializedName("relatedArticles") val relatedArticles: List<HelpArticleItem> = emptyList(),
    @SerializedName("helpfulYesCount") val helpfulYesCount: Int = 0,
    @SerializedName("helpfulNoCount") val helpfulNoCount: Int = 0,
    @SerializedName("helpfulPercent") val helpfulPercent: Int? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("createdBy") val createdBy: Any? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("slug") val slug: String = ""
)

data class HelpArticleSection(
    @SerializedName("heading") val heading: String = "",
    @SerializedName("body") val body: String? = null,
    @SerializedName("steps") val steps: List<String> = emptyList(),
    @SerializedName("note") val note: String? = null
)

data class HelpArticleSingleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: HelpArticleItem? = null,
    @SerializedName("message") val message: String? = null
)
