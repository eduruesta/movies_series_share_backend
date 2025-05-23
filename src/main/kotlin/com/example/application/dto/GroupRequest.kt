package com.example.application.dto

data class GroupRequest(
    val name: String,
    val description: String = "",
    val createdBy: String,
    val creatorName: String,
    val creatorEmail: String
)
