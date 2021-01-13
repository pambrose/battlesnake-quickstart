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

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package io.battlesnake.core

import io.ktor.application.*
import kotlin.time.Duration

abstract class AbstractGameStrategy<T : SnakeContext>(private val verbose: Boolean = false) : GameStrategy<T>() {

  init {
    onDescribe { call: ApplicationCall ->
      logger.info { describeMsg(call) }
      onDescribe(call)
    }

    onStart { context: T, request: StartRequest ->
      logger.info { startMsg(context, request) }
      onStart(context, request)
    }

    onMove { context: T, request: MoveRequest -> onMove(context, request) }

    onEnd { context: T, request: EndRequest ->
      logger.info { endMsg(context, request) }
      onEnd(context, request)
    }

    onAfterTurn { context: T?, call: ApplicationCall, gameResponse: GameResponse, duration: Duration ->
      if (verbose)
        logger.info { afterTurnMsg(context, call, gameResponse, duration) }
      onAfterTurn(gameResponse, duration)
    }
  }

  open fun onDescribe(call: ApplicationCall) = DescribeResponse()

  open fun onStart(context: T, request: StartRequest) {
    return
  }

  abstract fun onMove(context: T, request: MoveRequest): MoveResponse

  open fun onEnd(context: T, request: EndRequest) = EndResponse()

  open fun onAfterTurn(response: GameResponse, duration: Duration) {}
}