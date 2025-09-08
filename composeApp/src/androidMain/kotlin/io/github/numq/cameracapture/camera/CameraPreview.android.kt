package io.github.numq.cameracapture.camera

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.koin.compose.koinInject

@Composable
actual fun CameraPreview(modifier: Modifier) {
    val previewView = koinInject<PreviewView>()

    AndroidView(factory = { previewView }, modifier = modifier)
}