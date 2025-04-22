package com.example.domain.port

import com.example.domain.entity.Critics

interface CriticsRepository {
    suspend fun findAll(): List<Critics>
    suspend fun save(critics: Critics): Critics
}