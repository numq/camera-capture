package io.github.numq.cameracapture.di

import io.github.numq.cameracapture.camera.*
import io.github.numq.cameracapture.interaction.*
import io.github.numq.cameracapture.navigation.NavigationFeature
import io.github.numq.cameracapture.navigation.NavigationReducer
import io.github.numq.cameracapture.server.*
import io.github.numq.cameracapture.settings.GetSettings
import io.github.numq.cameracapture.settings.Settings
import io.github.numq.cameracapture.settings.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose

private const val DEFAULT_HOST = "0.0.0.0"

private const val DEFAULT_PORT = 8090

private val DEFAULT_FLASHLIGHT_STATE = FlashlightState.DISABLED

private val DEFAULT_LENS_FACING = LensFacing.BACK

private val camera = module {
    single { CameraRepository.Default(cameraService = get(), settingsDataSource = get()) } bind CameraRepository::class

    factory { GetCameraState(cameraRepository = get()) }

    factory { ToggleFlashlight(cameraRepository = get()) }

    factory { SwitchCamera(cameraRepository = get()) }
}

private val navigation = module {
    single { NavigationReducer(getSettings = get()) }

    single { NavigationFeature(reducer = get()) } onClose {
        it?.close()
    }
}

private val interaction = module {
    single { CameraInteractionReducer(getCameraState = get(), toggleFlashlight = get(), switchCamera = get()) }

    single { ServerInteractionReducer(getServerState = get(), startServer = get(), stopServer = get()) }

    single { DialogInteractionReducer(changeHost = get(), changePort = get()) }

    single {
        InteractionReducer(
            cameraInteractionReducer = get(),
            serverInteractionReducer = get(),
            dialogInteractionReducer = get(),
        )
    }

    single { (initialSettings: Settings) ->
        InteractionFeature(initialSettings = initialSettings, reducer = get())
    } onClose {
        it?.close()
    }
}

private val server = module {
    single { ServerService.HttpServerService() } bind ServerService::class onClose {
        runBlocking {
            it?.close()?.getOrDefault(Unit)
        }
    }

    single {
        ServerRepository.Default(cameraService = get(), serverService = get(), settingsDataSource = get())
    } bind ServerRepository::class onClose {
        it?.close()
    }

    factory { StartServer(serverRepository = get()) }

    factory { StopServer(serverRepository = get()) }

    factory { ChangeHost(serverRepository = get()) }

    factory { ChangePort(serverRepository = get()) }

    factory { GetServerState(serverRepository = get()) }
}

private val settings = module {
    single {
        SettingsRepository.Default(
            defaultHost = DEFAULT_HOST,
            defaultPort = DEFAULT_PORT,
            defaultFlashlightState = DEFAULT_FLASHLIGHT_STATE,
            defaultLensFacing = DEFAULT_LENS_FACING,
            settingsDataSource = get(),
        )
    } bind SettingsRepository::class

    factory { GetSettings(settingsRepository = get()) }
}

val commonModule = listOf(camera, navigation, interaction, server, settings)