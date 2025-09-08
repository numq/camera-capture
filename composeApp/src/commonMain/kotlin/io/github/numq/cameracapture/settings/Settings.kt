package io.github.numq.cameracapture.settings

import io.github.numq.cameracapture.camera.FlashlightState
import io.github.numq.cameracapture.camera.LensFacing

data class Settings(val host: String, val port: Int, val flashlightState: FlashlightState, val lensFacing: LensFacing)