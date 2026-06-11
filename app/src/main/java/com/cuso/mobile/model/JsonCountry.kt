package com.cuso.mobile.model

data class JsonCountry(
    val name: String,
    val states: List<JsonState>
)

data class JsonState(
    val name: String
)