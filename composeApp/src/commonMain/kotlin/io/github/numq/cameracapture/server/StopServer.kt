package io.github.numq.cameracapture.server

import io.github.numq.cameracapture.usecase.UseCase

class StopServer(private val serverRepository: ServerRepository) : UseCase<Unit, Unit> {
    override suspend fun execute(input: Unit) = serverRepository.stop()
}