package com.example.application.dto

import com.example.domain.entity.Critics

data class CriticsResponse(
    val id: Long,
    val title: String,
    val rating: Float,
    val comments: List<CommentResponse>,
    val imageUrl: String,
    val genre: String,
    val platform: String,
    val year: String,
    val duration: String,
    val contentRating: String,
    val synopsis: String,
    val posterUrl: String?,
    val ratingCount: Int,
    val averageRating: Float,
    val backdropUrl: String?,
    val groupId: String?,
    val username: String,
    val mediaType: String?
)

fun Critics.toResponse(): CriticsResponse {
    return CriticsResponse(
        id = id,
        title = title,
        rating = rating,
        comments = comments.map { it.toResponse() },
        imageUrl = imageUrl,
        genre = genre,
        platform = platform,
        year = year,
        duration = duration,
        contentRating = contentRating,
        synopsis = synopsis,
        posterUrl = posterUrl,
        ratingCount = ratingCount,
        averageRating = averageRating,
        backdropUrl = backdropUrl,
        groupId = groupId,
        username = username,
        mediaType = mediaType
    )
}
