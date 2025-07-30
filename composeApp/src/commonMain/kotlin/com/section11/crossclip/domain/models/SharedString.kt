package com.section11.crossclip.domain.models

data class SharedString(
    val id: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val userId: String = "",
    val deviceInfo: String = ""
)
