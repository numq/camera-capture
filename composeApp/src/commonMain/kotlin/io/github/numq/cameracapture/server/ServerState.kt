package io.github.numq.cameracapture.server

sealed interface ServerState {
    val name: String

    data class Error(val exception: Exception) : ServerState {
        override val name = "Error"
    }

    data object Connecting : ServerState {
        override val name = "Connecting"
    }

    data class Connected(val host: String, val port: Int) : ServerState {
        override val name = "Connected"
    }

    data object Disconnecting : ServerState {
        override val name = "Disconnecting"
    }

    data object Disconnected : ServerState {
        override val name = "Disconnected"
    }
}