package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.camera.CameraState
import io.github.numq.cameracapture.server.ServerState

data class InteractionState(
    val host: String,
    val port: Int,
    val serverState: ServerState,
    val cameraState: CameraState,
    val isDialogVisible: Boolean = false
)