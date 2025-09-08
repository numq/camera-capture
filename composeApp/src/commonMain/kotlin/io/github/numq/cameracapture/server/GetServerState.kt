package io.github.numq.cameracapture.server

import io.github.numq.cameracapture.usecase.UseCase
import kotlinx.coroutines.flow.StateFlow

class GetServerState(private val serverRepository: ServerRepository) : UseCase<Unit, StateFlow<ServerState>> {
    override suspend fun execute(input: Unit) = serverRepository.getServerState()
}