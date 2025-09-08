package io.github.numq.cameracapture.server

import io.github.numq.cameracapture.usecase.UseCase

class StartServer(private val serverRepository: ServerRepository) : UseCase<StartServer.Input, Unit> {
    data class Input(val host: String, val port: Int)

    override suspend fun execute(input: Input) = with(input) {
        serverRepository.start(host = host, port = port)
    }
}