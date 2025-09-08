package io.github.numq.cameracapture.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.numq.cameracapture.camera.FlashlightState
import io.github.numq.cameracapture.camera.LensFacing
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

actual interface SettingsDataSource {
    actual suspend fun getHost(): Result<String?>

    actual suspend fun updateHost(host: String): Result<Unit>

    actual suspend fun getPort(): Result<Int?>

    actual suspend fun updatePort(port: Int): Result<Unit>

    actual suspend fun getFlashlightState(): Result<FlashlightState?>

    actual suspend fun updateFlashlightState(flashlightState: FlashlightState): Result<Unit>

    actual suspend fun getLensFacing(): Result<LensFacing?>

    actual suspend fun updateLensFacing(lensFacing: LensFacing): Result<Unit>

    class LocalSettingsDataSource(private val dataStore: DataStore<Preferences>) : SettingsDataSource {
        object Keys {
            val HOST = stringPreferencesKey("host")

            val PORT = intPreferencesKey("port")

            val FLASHLIGHT_STATE = intPreferencesKey("flashlight_state")

            val LENS_FACING = intPreferencesKey("lens_facing")
        }

        override suspend fun getHost() = runCatching {
            dataStore.data.map { preferences -> preferences[Keys.HOST] }.firstOrNull()
        }

        override suspend fun updateHost(host: String) = runCatching {
            dataStore.edit { preferences -> preferences[Keys.HOST] = host }

            Unit
        }

        override suspend fun getPort() = runCatching {
            dataStore.data.map { preferences -> preferences[Keys.PORT] }.firstOrNull()
        }

        override suspend fun updatePort(port: Int) = runCatching {
            dataStore.edit { preferences -> preferences[Keys.PORT] = port }

            Unit
        }

        override suspend fun getFlashlightState() = runCatching {
            dataStore.data.map { preferences -> preferences[Keys.FLASHLIGHT_STATE] }.firstOrNull()?.let { index ->
                FlashlightState.entries[index]
            }
        }

        override suspend fun updateFlashlightState(flashlightState: FlashlightState) = runCatching {
            dataStore.edit { preferences -> preferences[Keys.FLASHLIGHT_STATE] = flashlightState.ordinal }

            Unit
        }

        override suspend fun getLensFacing() = runCatching {
            dataStore.data.map { preferences -> preferences[Keys.LENS_FACING] }.firstOrNull()?.let { index ->
                LensFacing.entries[index]
            }
        }

        override suspend fun updateLensFacing(lensFacing: LensFacing) = runCatching {
            dataStore.edit { preferences -> preferences[Keys.LENS_FACING] = lensFacing.ordinal }

            Unit
        }
    }
}