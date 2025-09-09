package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.camera.CameraState
import io.github.numq.cameracapture.server.ServerState

sealed interface InteractionCommand {
    sealed interface Camera : InteractionCommand {
        data class HandleCameraState(val cameraState: CameraState) : Camera

        data object Start : Camera

        data object Stop : Camera

        data object ToggleFlashlight : Camera

        data object Switch : Camera
    }

    sealed interface Dialog : InteractionCommand {
        data object Open : Dialog

        data class Close(val port: Int) : Dialog
    }

    sealed interface Server : InteractionCommand {
        data class HandleServerState(val serverState: ServerState) : Server

        data object Start : Server

        data object Stop : Server

        data object Restart : Server
    }

    data class HandleError(val throwable: Throwable) : InteractionCommand
}