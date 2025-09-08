package io.github.numq.cameracapture.navigation

import io.github.numq.cameracapture.feature.Reducer
import io.github.numq.cameracapture.settings.GetSettings
import io.github.numq.cameracapture.throwable.exception

class NavigationReducer(
    private val getSettings: GetSettings
) : Reducer<NavigationCommand, NavigationState, NavigationEvent> {
    override suspend fun reduce(
        state: NavigationState,
        command: NavigationCommand
    ) = when (command) {
        is NavigationCommand.Initialize -> getSettings.execute(Unit).fold(onSuccess = { settings ->
            transition(NavigationState.Interaction(initialSettings = settings))
        }, onFailure = { throwable ->
            transition(NavigationState.Error(exception = throwable.exception))
        })
    }
}