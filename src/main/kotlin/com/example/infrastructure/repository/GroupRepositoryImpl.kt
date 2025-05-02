package com.example.infrastructure.repository

import com.example.domain.entity.Group
import com.example.domain.port.GroupRepository
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class GroupRepositoryImpl(private val mongoDatabase: MongoDatabase) : GroupRepository {

    override suspend fun findAll(): List<Group> =
        mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
            .find()
            .toList()

    override suspend fun findById(id: String): Group? =
        mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
            .find(eq("_id", id))
            .firstOrNull()
            
    override suspend fun findByInviteCode(inviteCode: String): Group? =
        mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
            .find(eq("inviteCode", inviteCode))
            .firstOrNull()

    override suspend fun save(group: Group): Group {
        mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
            .insertOne(group)
        return group
    }

    override suspend fun update(id: String, group: Group): Group? {
        val collection = mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
        val updatedGroup = group.copy(id = id)
        val result = collection.replaceOne(
            eq("_id", id),
            updatedGroup,
            ReplaceOptions().upsert(false)
        )
        
        return if (result.modifiedCount > 0) {
            updatedGroup
        } else {
            null
        }
    }

    override suspend fun delete(id: String): Boolean {
        val result = mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
            .deleteOne(eq("_id", id))
        return result.deletedCount > 0
    }

    override suspend fun addMember(groupId: String, memberName: String): Group? {
        val group = findById(groupId) ?: return null
        
        // Verificar si el miembro ya está en el grupo
        if (memberName in group.members) {
            return group
        }
        
        // Añadir el miembro a la lista
        val updatedMembers = group.members + memberName
        val updatedGroup = group.copy(members = updatedMembers)
        
        return update(groupId, updatedGroup)
    }

    companion object {
        const val GROUPS_COLLECTION = "groups"
    }
}
