package com.example.application.routes

import com.example.application.dto.GroupRequest
import com.example.application.dto.JoinGroupRequest
import com.example.application.dto.toResponse
import com.example.domain.entity.Group
import com.example.domain.port.GroupRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.groups() {
    val groupRepository: GroupRepository by inject()

    route("/groups") {
        get {
            try {
                val groups = groupRepository.findAll()
                val response = groups.map { it.toResponse() }
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al obtener los grupos"))
                )
            }
        }

        post {
            try {
                val request = call.receive<GroupRequest>()
                
                val group = Group(
                    name = request.name,
                    description = request.description,
                    createdBy = request.createdBy,
                    members = listOf(request.createdBy) // El creador es el primer miembro
                )
                
                val savedGroup = groupRepository.save(group)
                call.respond(HttpStatusCode.Created, savedGroup.toResponse())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Error desconocido al crear el grupo"))
                )
            }
        }

        get("/{id}") {
            try {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))
                
                val group = groupRepository.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Grupo no encontrado"))
                
                call.respond(group.toResponse())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al buscar el grupo"))
                )
            }
        }

        post("/join/{inviteCode}") {
            try {
                val inviteCode = call.parameters["inviteCode"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Código de invitación inválido"))
                
                val request = call.receive<JoinGroupRequest>()
                
                val group = groupRepository.findByInviteCode(inviteCode)
                    ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Grupo no encontrado"))
                
                val updatedGroup = groupRepository.addMember(group.id, request.memberName)
                    ?: return@post call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al unirse al grupo"))
                
                call.respond(updatedGroup.toResponse())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al unirse al grupo"))
                )
            }
        }
    }
}
