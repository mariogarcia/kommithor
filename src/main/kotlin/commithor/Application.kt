package commithor

import java.io.File

import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter

import io.ktor.server.netty.Netty
import io.ktor.server.engine.embeddedServer

import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.content.static
import io.ktor.http.content.resources

import io.ktor.jackson.jackson
import io.ktor.features.ContentNegotiation
import com.fasterxml.jackson.databind.SerializationFeature
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import commithor.git.getSlackersFrom
import java.text.SimpleDateFormat

import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Application's main entry point
 *
 * @since 1.0-SNAPSHOT
 **/
fun Application.main() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            dateFormat = SimpleDateFormat("yyyy-MM-dd")
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

            val username: String = config
                    .property("username")
                    .getString()

            val password: String = config
                    .property("password")
                    .getString()

            val credentials = UsernamePasswordCredentialsProvider(username, password)

            call.respond(getSlackersFrom(repository, tempDir, credentials))
        }

        get("/sse") {
            call.respondSse(channel.openSubscription())
        }
    }
}

val channel = produce { // this: ProducerScope<SseEvent> ->
            var n = 0
            while (true) {
                send(SseEvent("demo$n"))
                delay(1000)
                n++
            }
        }.broadcast()

data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

val ContentTypeTextEventStream = ContentType.parse("text/event-stream")

/**
 * Method that responds an [ApplicationCall] by reading all the [SseEvent]s from the specified [events] [ReceiveChannel]
 * and serializing them in a way that is compatible with the Server-Sent Events specification.
 *
 * You can read more about it here: https://www.html5rocks.com/en/tutorials/eventsource/basics/
 */
suspend fun ApplicationCall.respondSse(events: ReceiveChannel<SseEvent>) {
    response.header(HttpHeaders.CacheControl, "no-cache")
    respondTextWriter(contentType = ContentTypeTextEventStream) {
        for (event in events) {
            if (event.id != null) {
                write("id: ${event.id}\n")
            }
            if (event.event != null) {
                write("event: ${event.event}\n")
            }
            for (dataLine in event.data.lines()) {
                write("data: $dataLine\n")
            }
            write("\n")
            flush()
        }
    }
}
