package com.gunggeumap.ggm.data.model

data class ApiResult<T>(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: T?
)
