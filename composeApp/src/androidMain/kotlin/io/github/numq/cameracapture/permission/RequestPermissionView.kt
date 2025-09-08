package io.github.numq.cameracapture.permission

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission(
    multiplePermissionsState: MultiplePermissionsState,
    onRequestPermission: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            multiplePermissionsState.shouldShowRationale -> {
                Text(
                    "Please grant permission to use the camera to use the app's functionality",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onRequestPermission) {
                    Text("Grant permission")
                }
            }

            !multiplePermissionsState.allPermissionsGranted && !multiplePermissionsState.shouldShowRationale -> {
                Button(onClick = onRequestPermission) {
                    Text("Allow access to camera")
                }
            }
        }
    }
}