package io.github.numq.cameracapture.settings

import io.github.numq.cameracapture.camera.FlashlightState
import io.github.numq.cameracapture.camera.LensFacing

interface SettingsRepository {
    suspend fun getSettings(): Result<Settings>

    class Default(
        private val defaultHost: String,
        private val defaultPort: Int,
        private val defaultFlashlightState: FlashlightState,
        private val defaultLensFacing: LensFacing,
        private val settingsDataSource: SettingsDataSource
    ) : SettingsRepository {
        override suspend fun getSettings() = settingsDataSource.runCatching {
            Settings(
                host = getHost().getOrThrow() ?: defaultHost,
                port = getPort().getOrThrow() ?: defaultPort,
                flashlightState = getFlashlightState().getOrThrow() ?: defaultFlashlightState,
                lensFacing = getLensFacing().getOrThrow() ?: defaultLensFacing,
            )
        }
    }
}