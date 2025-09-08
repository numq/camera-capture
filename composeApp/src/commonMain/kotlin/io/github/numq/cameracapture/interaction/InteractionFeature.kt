package io.github.numq.cameracapture.interaction

import io.github.numq.cameracapture.camera.CameraState
import io.github.numq.cameracapture.feature.Feature
import io.github.numq.cameracapture.server.ServerState
import io.github.numq.cameracapture.settings.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.ext.getFullName

class InteractionFeature(
    initialSettings: Settings, reducer: InteractionReducer
) : Feature<InteractionCommand, InteractionState, InteractionEvent>(
    initialState = InteractionState(
        host = initialSettings.host,
        port = initialSettings.port,
        serverState = ServerState.Disconnected,
        cameraState = CameraState.Inactive
    ), coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()), reducer = reducer
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private val jobs = mutableMapOf<String, Job>()

    init {
        coroutineScope.launch {
            events.collect { event ->
                val key = event::class.getFullName()

                jobs[key]?.cancel()

                when (event) {
                    is InteractionEvent.CapturingStarted -> event.serverState.onEach { serverState ->
                        execute(InteractionCommand.Server.HandleServerState(serverState = serverState))
                    }.launchIn(this)

                    is InteractionEvent.CapturingStopped -> coroutineScope.launch {
                        jobs[InteractionEvent.CapturingStarted::class.getFullName()]?.cancel()
                    }

                    is InteractionEvent.CameraStarted -> event.cameraState.onEach { cameraState ->
                        execute(InteractionCommand.Camera.HandleCameraState(cameraState = cameraState))
                    }.launchIn(this)

                    is InteractionEvent.CameraStopped -> coroutineScope.launch {
                        jobs[InteractionEvent.CameraStarted::class.getFullName()]?.cancel()
                    }

                    is InteractionEvent.RestartRequested -> coroutineScope.launch {
                        execute(InteractionCommand.Server.Restart)
                    }

                    else -> null
                }?.let { job ->
                    jobs[key] = job
                }
            }
        }

        coroutineScope.launch {
            execute(InteractionCommand.Camera.Start)
            execute(InteractionCommand.Server.Start)
        }

        invokeOnClose {
            runBlocking {
                withContext(NonCancellable) {
                    execute(InteractionCommand.Camera.Stop)
                    execute(InteractionCommand.Server.Stop)
                }
            }

            coroutineScope.cancel()
        }
    }
}