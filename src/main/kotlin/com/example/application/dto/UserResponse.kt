package com.example.application.dto

import com.example.domain.entity.User

data class UserResponse(
    val id: String,
    val name: String,
    val email: String
)

fun User.toResponse(): UserResponse {
    return UserResponse(
        id = id,
        name = name,
        email = email
    )
}
