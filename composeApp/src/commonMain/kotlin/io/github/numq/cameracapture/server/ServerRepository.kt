package io.github.numq.cameracapture.server

import androidx.datastore.core.Closeable
import io.github.numq.cameracapture.camera.CameraService
import io.github.numq.cameracapture.camera.CameraState
import io.github.numq.cameracapture.settings.SettingsDataSource
import io.github.numq.cameracapture.throwable.exception
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import kotlin.io.encoding.Base64

interface ServerRepository : Closeable {
    suspend fun changeHost(host: String): Result<Unit>

    suspend fun changePort(port: Int): Result<Unit>

    suspend fun getServerState(): Result<StateFlow<ServerState>>

    suspend fun start(host: String, port: Int): Result<Unit>

    suspend fun stop(): Result<Unit>

    class Default(
        private val cameraService: CameraService,
        private val serverService: ServerService,
        private val settingsDataSource: SettingsDataSource
    ) : ServerRepository {
        private val coroutineScope = CoroutineScope(Dispatchers.Default)

        private var exchangeJob: Job? = null

        override suspend fun changeHost(host: String) = settingsDataSource.updateHost(host)

        override suspend fun changePort(port: Int) = settingsDataSource.updatePort(port)

        override suspend fun getServerState() = Result.success(serverService.serverState)

        override suspend fun start(host: String, port: Int) = runCatching {
            if (exchangeJob == null) {
                serverService.start(host = host, port = port).getOrThrow()

                exchangeJob = serverService.serverExchange.onEach { exchange ->
                    when (exchange.request.endpoint) {
                        is ServerEndpoint.Capture -> {
                            if (cameraService.cameraState.value is CameraState.Active) {
                                val json = cameraService.takePicture().fold(onSuccess = { cameraPicture ->
                                    with(cameraPicture) {
                                        JSONObject().apply {
                                            put("status", "success")
                                            put("image_bytes", Base64.encode(bytes))
                                            put("width", width)
                                            put("height", height)
                                        }
                                    }
                                }, onFailure = { throwable ->
                                    JSONObject().apply {
                                        put("status", "failure")
                                        put("error", throwable.exception.toString())
                                    }
                                })

                                exchange.onResponse(data = json.toString())
                            }
                        }
                    }
                }.launchIn(coroutineScope)
            }
        }

        override suspend fun stop() = runCatching {
            if (exchangeJob != null) {
                exchangeJob?.cancel()

                exchangeJob = null

                serverService.stop().getOrThrow()
            }
        }

        override fun close() {
            coroutineScope.cancel()
        }
    }
}