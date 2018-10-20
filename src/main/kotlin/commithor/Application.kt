package commithor

import io.ktor.routing.get
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

import commithor.data.slackers
// import commithor.git.getSlackers

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
        get("/api") {
            // call.respond(getSlackers())
            call.respond(slackers)
        }
    }
}
