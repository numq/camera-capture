package io.github.numq.cameracapture.camera

import io.github.numq.cameracapture.usecase.UseCase

class ToggleFlashlight(private val cameraRepository: CameraRepository) : UseCase<Unit, Unit> {
    override suspend fun execute(input: Unit) = cameraRepository.getCameraState().mapCatching { cameraState ->
        when (val state = cameraState.value) {
            is CameraState.Active -> {
                val flashlightState = when (state.flashlightState) {
                    FlashlightState.DISABLED -> FlashlightState.ENABLED

                    FlashlightState.ENABLED -> FlashlightState.DISABLED
                }

                cameraRepository.setFlashlightState(flashlightState = flashlightState).getOrThrow()
            }

            else -> Unit
        }
    }
}