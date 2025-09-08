package io.github.numq.cameracapture.server

import kotlinx.coroutines.CompletableDeferred

sealed interface ServerExchange {
    val request: ServerRequest

    suspend fun onResponse(data: Any)

    data class Text(
        override val request: ServerRequest,
        val handleText: CompletableDeferred<String>
    ) : ServerExchange {
        override suspend fun onResponse(data: Any) {
            runCatching {
                require(data is String) { "Expected String, got ${data::class.simpleName}" }

                data
            }.fold(onSuccess = handleText::complete, onFailure = handleText::completeExceptionally)
        }
    }
}