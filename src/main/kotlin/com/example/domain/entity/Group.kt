package com.example.domain.entity

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class Group(
    @BsonId
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<User> = emptyList(),
    val inviteCode: String = generateRandomCode()
)

private fun generateRandomCode(): String {
    return UUID.randomUUID().toString().substring(0, 8)
}
