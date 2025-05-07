package com.example.domain.port

import com.example.domain.entity.Group
import com.example.domain.entity.User

interface GroupRepository {
    suspend fun findAll(): List<Group>
    suspend fun findById(id: String): Group?
    suspend fun findByInviteCode(inviteCode: String): Group?
    suspend fun save(group: Group): Group
    suspend fun update(id: String, group: Group): Group?
    suspend fun delete(id: String): Boolean
    suspend fun addMember(groupId: String, user: User): Group?
    suspend fun findGroupsByMemberId(memberId: String): List<Group>
}
