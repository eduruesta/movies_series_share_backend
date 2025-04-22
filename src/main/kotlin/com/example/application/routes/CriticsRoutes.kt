package com.example.application.routes

import com.example.application.dto.toResponse
import com.example.domain.entity.Critics
import com.example.domain.port.CriticsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.critics() {
    val criticsRepository: CriticsRepository by inject()

    route("/critics") {
        get {
            val teamList = criticsRepository.findAll()
            val response = teamList.map { it.toResponse() }
            call.respond(response)
        }
        
        post {
            try {
                val request = call.receive<Critics>()
                val savedCritic = criticsRepository.save(request)
                call.respond(HttpStatusCode.Created, savedCritic.toResponse())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to (e.message ?: "Error desconocido al crear la crítica"))
                )
            }
        }
    }
}
