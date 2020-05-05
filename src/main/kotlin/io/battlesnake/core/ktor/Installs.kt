package io.battlesnake.core.ktor

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun Application.installs() {
  install(Compression) {
    gzip {
      priority = 1.0
    }
    deflate {
      priority = 10.0
      minimumSize(1024) // condition
    }
  }

  install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/") }
  }

  install(ContentNegotiation) {
    json(contentType = ContentType.Application.Json,
         json = Json(DefaultJsonConfiguration.copy(ignoreUnknownKeys = true, prettyPrint = true)))
  }

  install(StatusPages) {
    exception<AuthenticationException> { cause ->
      call.respond(HttpStatusCode.Unauthorized)
    }
    exception<AuthorizationException> { cause ->
      call.respond(HttpStatusCode.Forbidden)
    }
  }

}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
