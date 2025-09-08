package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.camera.CameraState
import io.github.numq.cameracapture.event.Event
import io.github.numq.cameracapture.server.ServerState
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class InteractionEvent private constructor() : Event<Uuid> {
    override val key = Uuid.random()

    data class Error(val exception: Exception) : InteractionEvent()

    data class CapturingStarted(val serverState: StateFlow<ServerState>) : InteractionEvent()

    data object CapturingStopped : InteractionEvent()

    data class CameraStarted(val cameraState: StateFlow<CameraState>) : InteractionEvent()

    data object CameraStopped : InteractionEvent()

    data object RestartRequested : InteractionEvent()
}