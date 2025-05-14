package com.example.domain.port

import com.example.domain.entity.Critics

interface CriticsRepository {
    suspend fun findAll(): List<Critics>
    suspend fun save(critics: Critics): Critics
    suspend fun findById(id: Long): Critics?
    suspend fun update(id: Long, critics: Critics): Critics?
    suspend fun findByGroupIds(groupIds: List<String>): List<Critics>
}