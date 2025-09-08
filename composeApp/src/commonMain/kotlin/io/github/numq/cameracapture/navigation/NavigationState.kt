package io.github.numq.cameracapture.navigation

import io.github.numq.cameracapture.settings.Settings

sealed interface NavigationState {
    data object Initialization : NavigationState

    data class Interaction(val initialSettings: Settings) : NavigationState

    data class Error(val exception: Exception) : NavigationState
}