package io.github.numq.cameracapture.navigation

import io.github.numq.cameracapture.event.Event
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class NavigationEvent private constructor() : Event<Uuid> {
    override val key = Uuid.random()

    data class Error(val exception: Exception) : NavigationEvent()
}