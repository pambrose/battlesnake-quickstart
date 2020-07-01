/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
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
import io.battlesnake.core.END
import io.battlesnake.core.INFO
import io.battlesnake.core.MOVE
import io.battlesnake.core.START
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
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
        head {
        }

        body {
          br {}
          h2 { +"""You have reached a """; a { href = "https://docs.battlesnake.io"; +"Battlesnake" }; +" server!" }
          h3 {
            +"Use this value as your snake URL: "
            span { rawHtml(nbsp.text) }
            span { id = "url" }
          }
          script { rawHtml("""document.getElementById("url").innerHTML = window.location.href;""") }
        }
      }
    }

    get(DESCRIBE) {
      val response = snake.process(call)
      call.respond(response)
    }

    post(DESCRIBE) {
      val response = snake.process(call)
      call.respond(response)
    }

    post(START) {
      val response = snake.process(call)
      call.respond(response)
    }

    post(MOVE) {
      val response = snake.process(call)
      call.respond(response)
    }

    post(END) {
      val response = snake.process(call)
      call.respond(response)
    }
  }
}

private fun HTMLTag.rawHtml(html: String) = unsafe { raw(html) }