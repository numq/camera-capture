package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.feature.Reducer
import io.github.numq.cameracapture.feature.Transition
import io.github.numq.cameracapture.feature.mergeEvents
import io.github.numq.cameracapture.server.GetServerState
import io.github.numq.cameracapture.server.StartServer
import io.github.numq.cameracapture.server.StopServer

class ServerInteractionReducer(
    private val getServerState: GetServerState,
    private val startServer: StartServer,
    private val stopServer: StopServer
) : Reducer<InteractionCommand.Server, InteractionState, InteractionEvent> {
    override suspend fun reduce(
        state: InteractionState, command: InteractionCommand.Server
    ): Transition<InteractionState, InteractionEvent> = when (command) {
        is InteractionCommand.Server.HandleServerState -> transition(state.copy(serverState = command.serverState))

        is InteractionCommand.Server.Start -> runCatching {
            startServer.execute(StartServer.Input(host = state.host, port = state.port)).getOrThrow()

            getServerState.execute(Unit).getOrThrow()
        }.fold(onSuccess = { serverState ->
            transition(state, InteractionEvent.CapturingStarted(serverState = serverState))
        }, onFailure = { throwable ->
            transition(state)
        })

        is InteractionCommand.Server.Stop -> stopServer.execute(Unit).fold(onSuccess = {
            transition(state, InteractionEvent.CapturingStopped)
        }, onFailure = { throwable ->
            transition(state)
        })

        is InteractionCommand.Server.Restart -> {
            val (state, events) = reduce(state, InteractionCommand.Server.Stop)

            reduce(state, InteractionCommand.Server.Start).mergeEvents(events)
        }
    }
}