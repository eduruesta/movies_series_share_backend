package com.example.domain.entity

import org.bson.codecs.pojo.annotations.BsonId

data class Critics(
    @BsonId
    val id: Long = 0,
    val title: String = "",
    val rating: Float = 0f,
    val comments: List<Comment> = emptyList(),
    val imageUrl: String = "",
    val genre: String = "",
    val platform: String = "",
    val year: String = "",
    val duration: String = "",
    val contentRating: String = "",
    val synopsis: String = "",
    val posterUrl: String? = null,
    val ratingCount: Int = 1,
    val averageRating: Float = 0f,
    val backdropUrl: String? = null,
    val groupId: String? = null, // ID del grupo al que pertenece esta crítica (opcional)
    val username: String = "", // Username of the person who created the review
)
