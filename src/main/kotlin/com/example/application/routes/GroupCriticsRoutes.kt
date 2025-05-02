package com.example.application.routes

import com.example.application.dto.toResponse
import com.example.domain.entity.Critics
import com.example.domain.port.CriticsRepository
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

fun Route.groupCritics() {
    val criticsRepository: CriticsRepository by inject()
    val groupRepository: GroupRepository by inject()

    route("/groups/{groupId}/critics") {
        get {
            try {
                val groupId = call.parameters["groupId"] 
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de grupo inválido"))
                
                if (groupRepository.findById(groupId) == null) {
                    return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Grupo no encontrado"))
                }
                
                val allCritics = criticsRepository.findAll()
                val groupCritics = allCritics.filter { it.groupId == groupId }
                
                val response = groupCritics.map { it.toResponse() }
                call.respond(response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (e.message ?: "Error desconocido al obtener las críticas del grupo"))
                )
            }
        }
        
        post {
            try {
                val groupId = call.parameters["groupId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID de grupo inválido"))
                
                if (groupRepository.findById(groupId) == null) {
                    return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Grupo no encontrado"))
                }
                
                val criticRequest = call.receive<Critics>()
                val criticWithGroup = criticRequest.copy(groupId = groupId)
                
                val savedCritic = criticsRepository.save(criticWithGroup)
                call.respond(HttpStatusCode.Created, savedCritic.toResponse())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Error desconocido al crear la crítica en el grupo"))
                )
            }
        }
    }
}
