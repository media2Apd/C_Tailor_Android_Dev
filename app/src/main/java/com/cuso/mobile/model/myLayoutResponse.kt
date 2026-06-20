package com.cuso.mobile.model

data class myLayoutResponse(
    val success:String,
    val data:data
)
data class data(
    val initialized: Boolean,
    val layout:String?,
    val message:String
)
