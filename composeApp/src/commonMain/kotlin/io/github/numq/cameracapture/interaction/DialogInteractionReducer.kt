package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.feature.Reducer
import io.github.numq.cameracapture.feature.mergeEvents
import io.github.numq.cameracapture.server.ChangePort
import io.github.numq.cameracapture.throwable.exception

class DialogInteractionReducer(
    private val changePort: ChangePort
) : Reducer<InteractionCommand.Dialog, InteractionState, InteractionEvent> {
    override suspend fun reduce(state: InteractionState, command: InteractionCommand.Dialog) = when (command) {
        is InteractionCommand.Dialog.Open -> transition(state.copy(isDialogVisible = true))

        is InteractionCommand.Dialog.Close -> with(command) {
            when {
                port == state.port -> transition(state.copy(isDialogVisible = false))

                else -> runCatching {
                    if (port != state.port) {
                        changePort.execute(ChangePort.Input(port = port)).getOrThrow()
                    }
                }.fold(onSuccess = {
                    transition(state.copy(port = port, isDialogVisible = false)).mergeEvents(
                        InteractionEvent.RestartRequested
                    )
                }, onFailure = { throwable ->
                    transition(state.copy(isDialogVisible = false), InteractionEvent.Error(throwable.exception))
                })
            }
        }
    }
}