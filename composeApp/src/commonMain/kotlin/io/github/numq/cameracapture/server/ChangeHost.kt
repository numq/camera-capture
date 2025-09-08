package io.github.numq.cameracapture.server

import io.github.numq.cameracapture.usecase.UseCase

class ChangeHost(private val serverRepository: ServerRepository) : UseCase<ChangeHost.Input, Unit> {
    data class Input(val host: String)

    override suspend fun execute(input: Input) = serverRepository.changeHost(host = input.host)
}