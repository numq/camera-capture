package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.feature.Reducer
import io.github.numq.cameracapture.throwable.exception

class InteractionReducer(
    private val cameraInteractionReducer: CameraInteractionReducer,
    private val dialogInteractionReducer: DialogInteractionReducer,
    private val serverInteractionReducer: ServerInteractionReducer,
) : Reducer<InteractionCommand, InteractionState, InteractionEvent> {
    override suspend fun reduce(state: InteractionState, command: InteractionCommand) = when (command) {
        is InteractionCommand.Camera -> cameraInteractionReducer.reduce(state, command)

        is InteractionCommand.Dialog -> dialogInteractionReducer.reduce(state, command)

        is InteractionCommand.Server -> serverInteractionReducer.reduce(state, command)

        is InteractionCommand.HandleError -> transition(state, InteractionEvent.Error(command.throwable.exception))
    }
}