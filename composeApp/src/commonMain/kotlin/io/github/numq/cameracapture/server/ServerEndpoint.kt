package io.github.numq.cameracapture.server

sealed interface ServerEndpoint {
    val path: String

    data object Capture : ServerEndpoint {
        override val path = "/capture"
    }
}