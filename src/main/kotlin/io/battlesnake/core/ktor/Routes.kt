/*
 * Copyright © 2021 Paul Ambrose (pambrose@mac.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.battlesnake.core.ktor

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.DESCRIBE
import io.battlesnake.core.DescribeResponse
import io.battlesnake.core.END
import io.battlesnake.core.EndResponse
import io.battlesnake.core.INFO
import io.battlesnake.core.MOVE
import io.battlesnake.core.MoveResponse
import io.battlesnake.core.START
import io.battlesnake.core.StartResponse
import io.battlesnake.core.down
import io.battlesnake.core.left
import io.battlesnake.core.right
import io.battlesnake.core.up
import io.ktor.http.ContentType.Application.Json
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.Entities.nbsp
import kotlinx.html.HTMLTag
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.unsafe

fun Application.routes(snake: AbstractBattleSnake<*>) {
  routing {
    get(INFO) {
      call.respondHtml {
        head {}

        body {
          br {}
          h2 { +"""You have reached a """; a { href = "https://docs.battlesnake.io"; +"Battlesnake" }; +" server!" }
          h3 {
            +"Use this value as your snake URL: "
            span { rawHtml(nbsp.text) }
            span { id = "url" }
          }
          h3 {
            +"Snake type: ${snake.javaClass.name}"
          }
          script { rawHtml("""document.getElementById("url").innerHTML = window.location.href.slice(0, -5);""") }
        }
      }
    }

    get(DESCRIBE) {
      // We call response.toJson() to avoid ktor adding the "type" property to the json
      val response = snake.process(call) as DescribeResponse
      call.respondText(response.toJson(), Json)
    }

    post(DESCRIBE) {
      val response = snake.process(call) as DescribeResponse
      call.respondText(response.toJson(), Json)
    }

    post(START) {
      val response = snake.process(call) as StartResponse
      call.respondText(response.toJson(), Json)
    }

    post(MOVE) {
      val response = snake.process(call) as MoveResponse
      call.respondText(response.toJson(), Json)
    }

    post(END) {
      val response = snake.process(call) as EndResponse
      call.respondText(response.toJson(), Json)
    }

    get("/left") {
      val response = left("Going left")
      call.respondText(response.toJson(), Json)
    }

    get("/right") {
      val response = right("Going right")
      call.respondText(response.toJson(), Json)
    }

    get("/up") {
      val response = up("Going up")
      call.respondText(response.toJson(), Json)
    }

    get("/down") {
      val response = down("Going down")
      call.respondText(response.toJson(), Json)
    }
  }
}

private fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }