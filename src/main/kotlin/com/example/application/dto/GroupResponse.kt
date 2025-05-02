package com.example.application.dto

import com.example.domain.entity.Group

data class GroupResponse(
    val id: String,
    val name: String,
    val description: String,
    val createdBy: String,
    val createdAt: Long,
    val members: List<String>,
    val memberCount: Int,
    val inviteCode: String
)

fun Group.toResponse(): GroupResponse {
    return GroupResponse(
        id = id,
        name = name,
        description = description,
        createdBy = createdBy,
        createdAt = createdAt,
        members = members,
        memberCount = members.size,
        inviteCode = inviteCode
    )
}
