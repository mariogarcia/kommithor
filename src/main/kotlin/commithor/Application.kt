package commithor

import java.io.File

import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.respond
import io.ktor.response.respondText

import io.ktor.server.netty.Netty
import io.ktor.server.engine.embeddedServer

import io.ktor.http.ContentType
import io.ktor.http.content.static
import io.ktor.http.content.resources

import io.ktor.jackson.jackson
import io.ktor.features.ContentNegotiation
import com.fasterxml.jackson.databind.SerializationFeature

import commithor.git.getSlackersFrom

/**
 * Application's main entry point
 *
 * @since 1.0-SNAPSHOT
 **/
fun Application.main() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    routing {
        static("/") {
            resources("static")
        }

        get("/version") {
            val version: String = environment
                    .config
                    .config("commithor")
                    .property("version")
                    .getString()

            call.respond(mapOf("version" to version))
        }

        get("/slackers") {
            val config = environment.config.config("commithor")

            val tempDir: File = File(config
                    .property("tempDir")
                    .getString())

            val repository: String = config
                    .property("repository")
                    .getString()

            call.respond(getSlackersFrom(repository, tempDir))
        }
    }
}
