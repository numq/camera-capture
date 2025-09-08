package io.github.numq.cameracapture.di

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import io.github.numq.cameracapture.camera.CameraPreviewProvider
import io.github.numq.cameracapture.camera.CameraService
import io.github.numq.cameracapture.settings.SettingsDataSource
import io.github.numq.cameracapture.settings.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose

private val camera = module {
    single {
        val settingsRepository = get<SettingsRepository>()

        val initialFlashlightState = runBlocking {
            settingsRepository.getSettings().getOrThrow().flashlightState
        }

        val initialLensFacing = runBlocking {
            settingsRepository.getSettings().getOrThrow().lensFacing
        }

        CameraService.AndroidCameraService(
            initialFlashlightState = initialFlashlightState,
            initialLensFacing = initialLensFacing,
            context = get(),
            lifecycleOwner = get()
        )
    } bind CameraService::class onClose {
        runBlocking {
            it?.close()?.getOrDefault(Unit)
        }
    }

    single<CameraPreviewProvider> {
        get<CameraService.AndroidCameraService>()
    }

    single<PreviewView> {
        get<CameraPreviewProvider>().previewView
    }
}
private val settings = module {
    single {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                get<Context>().preferencesDataStoreFile("settings")
            })

        SettingsDataSource.LocalSettingsDataSource(dataStore = dataStore)
    } bind SettingsDataSource::class
}

val androidModule = listOf(camera, settings)