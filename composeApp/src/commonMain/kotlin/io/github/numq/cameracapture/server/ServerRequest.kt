package io.github.numq.cameracapture.server

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed interface ServerRequest {
    val endpoint: ServerEndpoint

    val timestamp: Duration

    data class Get(override val endpoint: ServerEndpoint) : ServerRequest {
        override val timestamp = System.currentTimeMillis().milliseconds
    }
}