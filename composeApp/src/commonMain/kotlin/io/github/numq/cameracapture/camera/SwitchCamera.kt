package io.github.numq.cameracapture.camera

import io.github.numq.cameracapture.usecase.UseCase

class SwitchCamera(private val cameraRepository: CameraRepository) : UseCase<Unit, Unit> {
    override suspend fun execute(input: Unit) = cameraRepository.getCameraState().mapCatching { cameraState ->
        when (val state = cameraState.value) {
            is CameraState.Active -> {
                val lensFacing = when (state.lensFacing) {
                    LensFacing.BACK -> LensFacing.FRONT

                    LensFacing.FRONT -> LensFacing.BACK
                }

                cameraRepository.setLensFacing(lensFacing = lensFacing).getOrThrow()
            }

            else -> Unit
        }
    }
}