package com.example.infrastructure.repository

import com.example.domain.entity.Critics
import com.example.domain.port.CriticsRepository
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList

class CriticsRepositoryImpl(private val mongoDatabase: MongoDatabase) : CriticsRepository {

    override suspend fun findAll(): List<Critics> =
        mongoDatabase.getCollection(CRITICS_COLLECTION, Critics::class.java)
            .find()
            .toList()

    override suspend fun save(critics: Critics): Critics {
        mongoDatabase.getCollection(CRITICS_COLLECTION, Critics::class.java)
            .insertOne(critics)
        return critics
    }

    companion object {
        const val CRITICS_COLLECTION = "critics"
    }
}
