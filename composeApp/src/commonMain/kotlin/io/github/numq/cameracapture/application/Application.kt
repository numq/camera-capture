package io.github.numq.cameracapture.application

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.numq.cameracapture.navigation.NavigationView
import io.github.numq.cameracapture.theme.ApplicationTheme

@Composable
fun Application() {
    ApplicationTheme(isDarkTheme = isSystemInDarkTheme()) {
        Surface {
            NavigationView()
        }
    }
}