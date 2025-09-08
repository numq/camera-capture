package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.feature.Reducer
import io.github.numq.cameracapture.feature.mergeEvents
import io.github.numq.cameracapture.server.ChangeHost
import io.github.numq.cameracapture.server.ChangePort
import io.github.numq.cameracapture.throwable.exception

class DialogInteractionReducer(
    private val changeHost: ChangeHost, private val changePort: ChangePort
) : Reducer<InteractionCommand.Dialog, InteractionState, InteractionEvent> {
    override suspend fun reduce(state: InteractionState, command: InteractionCommand.Dialog) = when (command) {
        is InteractionCommand.Dialog.Open -> transition(state.copy(isDialogVisible = true))

        is InteractionCommand.Dialog.Cancel -> transition(state.copy(isDialogVisible = false))

        is InteractionCommand.Dialog.Done -> with(command.copy(host = command.host.trim())) {
            when {
                host == state.host && port == state.port -> transition(state.copy(isDialogVisible = false))

                else -> runCatching {
                    if (host != state.host) {
                        changeHost.execute(ChangeHost.Input(host = host)).getOrThrow()
                    }
                    if (port != state.port) {
                        changePort.execute(ChangePort.Input(port = port)).getOrThrow()
                    }
                }.fold(onSuccess = {
                    transition(state.copy(host = host, port = port, isDialogVisible = false)).mergeEvents(
                        InteractionEvent.RestartRequested
                    )
                }, onFailure = { throwable ->
                    transition(state.copy(isDialogVisible = false), InteractionEvent.Error(throwable.exception))
                })
            }
        }
    }
}