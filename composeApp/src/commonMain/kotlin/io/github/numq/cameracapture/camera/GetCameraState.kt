package io.github.numq.cameracapture.camera

import io.github.numq.cameracapture.usecase.UseCase
import kotlinx.coroutines.flow.StateFlow

class GetCameraState(private val cameraRepository: CameraRepository) : UseCase<Unit, StateFlow<CameraState>> {
    override suspend fun execute(input: Unit) = cameraRepository.getCameraState()
}