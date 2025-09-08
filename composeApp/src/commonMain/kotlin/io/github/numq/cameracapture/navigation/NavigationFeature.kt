package io.github.numq.cameracapture.navigation

import io.github.numq.cameracapture.feature.Feature
import kotlinx.coroutines.*

class NavigationFeature(
    reducer: NavigationReducer
) : Feature<NavigationCommand, NavigationState, NavigationEvent>(
    initialState = NavigationState.Initialization,
    coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    reducer = reducer
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        coroutineScope.launch {
            execute(NavigationCommand.Initialize)
        }

        invokeOnClose {
            coroutineScope.cancel()
        }
    }
}