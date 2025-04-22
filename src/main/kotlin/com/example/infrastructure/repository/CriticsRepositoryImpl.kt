package com.example.infrastructure.repository

import com.example.domain.entity.Critics
import com.example.domain.port.CriticsRepository
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
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
    
    override suspend fun findById(id: Long): Critics? =
        mongoDatabase.getCollection(CRITICS_COLLECTION, Critics::class.java)
            .find(eq("_id", id))
            .firstOrNull()
    
    override suspend fun update(id: Long, critics: Critics): Critics? {
        val collection = mongoDatabase.getCollection(CRITICS_COLLECTION, Critics::class.java)
        val updatedCritics = critics.copy(id = id)
        val result = collection.replaceOne(
            eq("_id", id),
            updatedCritics,
            ReplaceOptions().upsert(false)
        )
        
        return if (result.modifiedCount > 0) {
            updatedCritics
        } else {
            null
        }
    }

    companion object {
        const val CRITICS_COLLECTION = "critics"
    }
}
