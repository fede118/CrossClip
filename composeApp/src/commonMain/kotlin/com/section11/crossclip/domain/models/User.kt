package com.section11.crossclip.domain.models

data class User(
    val idToken: String = "",
    val id: String = "",
    val email: String = "",
    val displayName: String? = "",
    val photoUrl: String? = null
)
