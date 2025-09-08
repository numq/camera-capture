package io.github.numq.cameracapture.settings

import io.github.numq.cameracapture.camera.FlashlightState
import io.github.numq.cameracapture.camera.LensFacing

expect interface SettingsDataSource {
    suspend fun getHost(): Result<String?>

    suspend fun updateHost(host: String): Result<Unit>

    suspend fun getPort(): Result<Int?>

    suspend fun updatePort(port: Int): Result<Unit>

    suspend fun getFlashlightState(): Result<FlashlightState?>

    suspend fun updateFlashlightState(flashlightState: FlashlightState): Result<Unit>

    suspend fun getLensFacing(): Result<LensFacing?>

    suspend fun updateLensFacing(lensFacing: LensFacing): Result<Unit>
}