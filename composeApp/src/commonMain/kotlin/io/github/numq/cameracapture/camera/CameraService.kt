package io.github.numq.cameracapture.camera

import kotlinx.coroutines.flow.StateFlow

expect interface CameraService {
    val cameraState: StateFlow<CameraState>

    suspend fun setFlashlightState(flashlightState: FlashlightState): Result<FlashlightState>

    suspend fun setLensFacing(lensFacing: LensFacing): Result<LensFacing>

    suspend fun takePicture(): Result<CameraPicture>

    suspend fun close(): Result<Unit>
}