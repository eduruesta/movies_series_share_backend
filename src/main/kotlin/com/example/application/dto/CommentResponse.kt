package com.example.application.dto

import com.example.domain.entity.Comment

data class CommentResponse(
    val text: String,
    val username: String
)

fun Comment.toResponse(): CommentResponse {
    return CommentResponse(
        text = text,
        username = username
    )
}
