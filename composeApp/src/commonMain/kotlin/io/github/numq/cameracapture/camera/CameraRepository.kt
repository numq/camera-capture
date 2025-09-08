package io.github.numq.cameracapture.camera

import io.github.numq.cameracapture.settings.SettingsDataSource
import kotlinx.coroutines.flow.StateFlow

interface CameraRepository {
    suspend fun getCameraState(): Result<StateFlow<CameraState>>

    suspend fun takePicture(): Result<CameraPicture>

    suspend fun setFlashlightState(flashlightState: FlashlightState): Result<Unit>

    suspend fun setLensFacing(lensFacing: LensFacing): Result<Unit>

    class Default(
        private val cameraService: CameraService, private val settingsDataSource: SettingsDataSource
    ) : CameraRepository {
        override suspend fun getCameraState() = Result.success(cameraService.cameraState)

        override suspend fun takePicture() = cameraService.takePicture()

        override suspend fun setFlashlightState(flashlightState: FlashlightState) = cameraService.setFlashlightState(
            flashlightState = flashlightState
        ).mapCatching { flashlightState ->
            settingsDataSource.updateFlashlightState(flashlightState = flashlightState).getOrThrow()
        }

        override suspend fun setLensFacing(lensFacing: LensFacing) = cameraService.setLensFacing(
            lensFacing = lensFacing
        ).mapCatching { lensFacing ->
            settingsDataSource.updateLensFacing(lensFacing = lensFacing).getOrThrow()
        }
    }
}