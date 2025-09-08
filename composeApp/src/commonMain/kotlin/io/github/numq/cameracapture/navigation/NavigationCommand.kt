package io.github.numq.cameracapture.navigation

sealed interface NavigationCommand {
    data object Initialize : NavigationCommand
}