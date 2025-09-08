package io.github.numq.cameracapture.server

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Job

internal sealed interface InternalServerState {
    data class Error(val exception: Exception) : InternalServerState

    data object Connecting : InternalServerState

    data class Connected(
        val host: String,
        val port: Int,
        val server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>,
        val job: Job
    ) : InternalServerState

    data object Disconnecting : InternalServerState

    data object Disconnected : InternalServerState
}