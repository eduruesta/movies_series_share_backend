package com.example.domain.entity

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class User(
    @BsonId
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val email: String = ""
)
