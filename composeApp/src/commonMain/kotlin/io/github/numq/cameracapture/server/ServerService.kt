package io.github.numq.cameracapture.server

import android.util.Log
import io.github.numq.cameracapture.throwable.exception
import io.ktor.http.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.net.BindException

interface ServerService {
    val serverState: StateFlow<ServerState>

    val serverExchange: Flow<ServerExchange>

    suspend fun start(host: String, port: Int): Result<Unit>

    suspend fun stop(): Result<Unit>

    suspend fun close(): Result<Unit>

    class HttpServerService : ServerService {
        private val coroutineScope = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            internalServerState.value = InternalServerState.Error(throwable.exception)
        })

        private val internalServerState = MutableStateFlow<InternalServerState>(InternalServerState.Disconnected)

        override val serverState = internalServerState.map { internalState ->
            when (internalState) {
                is InternalServerState.Error -> ServerState.Error(exception = internalState.exception)

                is InternalServerState.Connecting -> ServerState.Connecting

                is InternalServerState.Connected -> ServerState.Connected(
                    host = internalState.host, port = internalState.port
                )

                is InternalServerState.Disconnecting -> ServerState.Disconnecting

                is InternalServerState.Disconnected -> ServerState.Disconnected
            }
        }.stateIn(scope = coroutineScope, started = SharingStarted.Eagerly, initialValue = ServerState.Disconnected)

        private val _serverExchange = Channel<ServerExchange>(Channel.CONFLATED)

        override val serverExchange = _serverExchange.receiveAsFlow()

        init {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                if (throwable is BindException) {
                    Log.e("HttpServerService", "BindException caught", throwable)

                    internalServerState.value = InternalServerState.Error(throwable.exception)
                }
            }
        }

        override suspend fun start(host: String, port: Int) = runCatching {
            require(port in 0..65535) { "Port out of range: $port" }

            when (internalServerState.value) {
                is InternalServerState.Error, is InternalServerState.Disconnected -> {
                    internalServerState.value = InternalServerState.Connecting

                    runCatching {
                        embeddedServer(factory = CIO, host = host, port = port) {
                            routing {
                                get(ServerEndpoint.Capture.path) {
                                    val completableDeferred = CompletableDeferred<String>()

                                    _serverExchange.send(
                                        ServerExchange.Text(
                                            request = ServerRequest.Get(endpoint = ServerEndpoint.Capture),
                                            handleText = completableDeferred
                                        )
                                    )

                                    call.respondText(
                                        text = completableDeferred.await(), contentType = ContentType.Application.Json
                                    )
                                }
                            }
                        }
                    }.fold(onSuccess = { server ->
                        internalServerState.value = InternalServerState.Connected(
                            host = host,
                            port = port,
                            server = server,
                            job = coroutineScope.launch {
                                server.startSuspend(wait = true)
                            })
                    }, onFailure = { throwable ->
                        internalServerState.value = InternalServerState.Error(throwable.exception)
                    })
                }

                else -> Unit
            }
        }

        override suspend fun stop() = runCatching {
            when (val internalState = internalServerState.value) {
                is InternalServerState.Error, is InternalServerState.Connecting, is InternalServerState.Connected -> {
                    internalServerState.value = InternalServerState.Disconnecting

                    (internalState as? InternalServerState.Connected)?.apply {
                        job.cancel()

                        server.stopSuspend()
                    }

                    internalServerState.value = InternalServerState.Disconnected
                }

                else -> Unit
            }
        }

        override suspend fun close() = runCatching {
            coroutineScope.cancel()

            (internalServerState.value as? InternalServerState.Connected)?.apply {
                job.cancel()

                server.stop()
            }

            Unit
        }
    }
}