package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.camera.GetCameraState
import io.github.numq.cameracapture.camera.SwitchCamera
import io.github.numq.cameracapture.camera.ToggleFlashlight
import io.github.numq.cameracapture.feature.Reducer
import io.github.numq.cameracapture.throwable.exception

class CameraInteractionReducer(
    private val getCameraState: GetCameraState,
    private val toggleFlashlight: ToggleFlashlight,
    private val switchCamera: SwitchCamera,
) : Reducer<InteractionCommand.Camera, InteractionState, InteractionEvent> {
    override suspend fun reduce(state: InteractionState, command: InteractionCommand.Camera) = when (command) {
        is InteractionCommand.Camera.HandleCameraState -> transition(state.copy(cameraState = command.cameraState))

        is InteractionCommand.Camera.Start -> getCameraState.execute(Unit).fold(onSuccess = { cameraState ->
            transition(state, InteractionEvent.CameraStarted(cameraState = cameraState))
        }, onFailure = { throwable ->
            transition(state)
        })

        is InteractionCommand.Camera.Stop -> transition(state, InteractionEvent.CameraStopped)

        is InteractionCommand.Camera.ToggleFlashlight -> toggleFlashlight.execute(Unit).fold(onSuccess = {
            transition(state)
        }, onFailure = { throwable ->
            transition(state, InteractionEvent.Error(throwable.exception))
        })

        is InteractionCommand.Camera.Switch -> switchCamera.execute(Unit).fold(onSuccess = {
            transition(state)
        }, onFailure = { throwable ->
            transition(state, InteractionEvent.Error(throwable.exception))
        })
    }
}