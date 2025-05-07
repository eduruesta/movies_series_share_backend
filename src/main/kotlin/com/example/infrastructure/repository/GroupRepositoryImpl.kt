package com.example.infrastructure.repository

import com.example.domain.entity.Group
import com.example.domain.entity.User
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

    override suspend fun addMember(groupId: String, user: User): Group? {
        val group = findById(groupId) ?: return null
        
        // Verificar si el usuario ya está en el grupo
        if (group.members.any { it.id == user.id }) {
            return group
        }
        
        // Añadir el usuario a la lista de miembros
        val updatedMembers = group.members + user
        val updatedGroup = group.copy(members = updatedMembers)
        
        return update(groupId, updatedGroup)
    }
    
    override suspend fun findGroupsByMemberId(memberId: String): List<Group> =
        mongoDatabase.getCollection(GROUPS_COLLECTION, Group::class.java)
            .find(eq("members._id", memberId))
            .toList()

    companion object {
        const val GROUPS_COLLECTION = "groups"
    }
}
