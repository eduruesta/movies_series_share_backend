package com.example

import com.example.application.routes.critics
import com.example.application.routes.groups
import com.example.application.routes.groupCritics
import com.example.domain.port.CriticsRepository
import com.example.domain.port.GroupRepository
import com.example.infrastructure.codec.CriticsCodecProvider
import com.example.infrastructure.repository.CriticsRepositoryImpl
import com.example.infrastructure.repository.GroupRepositoryImpl
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing
import io.ktor.server.tomcat.jakarta.EngineMain
import org.bson.codecs.configuration.CodecRegistries
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.util.concurrent.TimeUnit


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        gson {
        }
    }
    install(CORS) {
        allowCredentials = true
        allowNonSimpleContentTypes = true
        anyHost()
    }
    install(Koin) {
        slf4jLogger()
        modules(module {
            single<MongoClient> {
                val uri = environment.config.propertyOrNull("ktor.mongo.uri")?.getString()
                    ?: throw RuntimeException("Failed to access MongoDB URI.")

                // Registramos el codec personalizado para la entidad Critics
                val codecRegistry = CodecRegistries.fromRegistries(
                    CodecRegistries.fromProviders(CriticsCodecProvider()),
                    MongoClientSettings.getDefaultCodecRegistry()
                )
                
                val settings = MongoClientSettings.builder()
                    .applyConnectionString(ConnectionString(uri))
                    .applyToSslSettings { it.invalidHostNameAllowed(true) }
                    .applyToSocketSettings { socketSettings ->
                        socketSettings.connectTimeout(30000, TimeUnit.MILLISECONDS) // 30 segundos
                        socketSettings.readTimeout(30000, TimeUnit.MILLISECONDS) // 30 segundos
                    }
                    .codecRegistry(codecRegistry)
                    .build()

                MongoClient.create(settings)
            }
            single {
                get<MongoClient>().getDatabase(
                    environment.config.property("ktor.mongo.database").getString()
                )
            }
        }, module {
            single<CriticsRepository> { CriticsRepositoryImpl(get()) }
            single<GroupRepository> { GroupRepositoryImpl(get()) }
        })
    }
    routing {
        swaggerUI(path = "swagger-ui", swaggerFile = "openapi/documentation.yaml") {
            version = "4.15.5"
        }
        critics()
        groups()
        groupCritics()
    }
}