package com.example.application.routes

import com.example.application.dto.GroupRequest
import com.example.application.dto.JoinGroupRequest
import com.example.application.dto.toResponse
import com.example.domain.entity.Group
import com.example.domain.entity.User
import com.example.domain.port.GroupRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
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
                
                // Crear un objeto User para el creador
                val creator = User(
                    id = request.createdBy,
                    name = request.creatorName,
                    email = request.creatorEmail
                )
                
                val group = Group(
                    name = request.name,
                    description = request.description,
                    createdBy = request.createdBy,
                    members = listOf(creator)
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
        
        // Nuevo endpoint para eliminar un grupo (solo el creador puede hacerlo)
        delete("/{id}") {
            try {
                val groupId = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de grupo inválido"))
                
                val userId = call.request.queryParameters["userId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de usuario requerido"))
                
                val group = groupRepository.findById(groupId)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Grupo no encontrado"))
                
                // Verificar si el usuario es el creador del grupo
                if (group.createdBy != userId) {
                    return@delete call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Solo el creador puede eliminar el grupo")
                    )
                }
                
                val success = groupRepository.delete(groupId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Grupo eliminado correctamente"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar el grupo"))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al eliminar el grupo"))
                )
            }
        }

        get("/by-member/{memberId}") {
            try {
                val memberId = call.parameters["memberId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de miembro inválido"))
                
                val groups = groupRepository.findGroupsByMemberId(memberId)
                val response = groups.map { it.toResponse() }
                
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al buscar grupos por miembro"))
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
                
                // Crear objeto User para el nuevo miembro
                val newMember = User(
                    id = request.userId,
                    name = request.userName,
                    email = request.userEmail
                )
                
                val updatedGroup = groupRepository.addMember(group.id, newMember)
                    ?: return@post call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al unirse al grupo"))
                
                call.respond(updatedGroup.toResponse())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al unirse al grupo"))
                )
            }
        }
        
        // Nuevo endpoint para salir de un grupo
        delete("/{groupId}/leave") {
            try {
                val groupId = call.parameters["groupId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de grupo inválido"))
                
                val userId = call.request.queryParameters["userId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de usuario requerido"))
                
                val group = groupRepository.findById(groupId)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Grupo no encontrado"))
                
                // Verificar si el usuario es miembro del grupo
                if (group.members.none { it.id == userId }) {
                    return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "El usuario no es miembro del grupo")
                    )
                }
                
                val updatedGroup = groupRepository.removeMember(groupId, userId)
                
                // Si el updatedGroup es null y el usuario era el creador, significa que el grupo fue eliminado
                if (updatedGroup == null && group.createdBy == userId) {
                    return@delete call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "Has abandonado el grupo. Como eras el último miembro, el grupo ha sido eliminado.")
                    )
                } else if (updatedGroup == null) {
                    return@delete call.respond(
                        HttpStatusCode.InternalServerError, 
                        mapOf("error" to "Error al salir del grupo")
                    )
                }
                
                // Si el usuario era el creador, informar que la propiedad ha sido transferida
                if (group.createdBy == userId) {
                    val newOwner = updatedGroup.members.first()
                    return@delete call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "message" to "Has abandonado el grupo. La propiedad ha sido transferida a ${newOwner.name}.",
                            "group" to updatedGroup.toResponse()
                        )
                    )
                }
                
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "message" to "Has abandonado el grupo exitosamente.",
                        "group" to updatedGroup.toResponse()
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al salir del grupo"))
                )
            }
        }
    }
}
