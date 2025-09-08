package io.github.numq.cameracapture.camera

sealed interface CameraState {
    data class Error(val exception: Exception) : CameraState

    data object Inactive : CameraState

    data class Active(val flashlightState: FlashlightState, val lensFacing: LensFacing) : CameraState
}