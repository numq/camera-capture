package io.github.numq.cameracapture.server

import io.github.numq.cameracapture.usecase.UseCase

class ChangePort(private val serverRepository: ServerRepository) : UseCase<ChangePort.Input, Unit> {
    data class Input(val port: Int)

    override suspend fun execute(input: Input) = serverRepository.changePort(port = input.port)
}