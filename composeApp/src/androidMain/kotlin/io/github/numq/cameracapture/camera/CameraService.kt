package io.github.numq.cameracapture.camera

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual interface CameraService {
    val previewView: PreviewView

    actual val cameraState: StateFlow<CameraState>

    actual suspend fun setFlashlightState(flashlightState: FlashlightState): Result<FlashlightState>

    actual suspend fun setLensFacing(lensFacing: LensFacing): Result<LensFacing>

    actual suspend fun takePicture(): Result<CameraPicture>

    actual suspend fun close(): Result<Unit>

    class AndroidCameraService(
        initialFlashlightState: FlashlightState,
        initialLensFacing: LensFacing,
        private val context: Context,
        private val lifecycleOwner: LifecycleOwner
    ) : CameraService, CameraPreviewProvider {
        private val executor = ContextCompat.getMainExecutor(context)

        private val cameraExecutor = Executors.newSingleThreadExecutor()

        private val flashlightStateRef = AtomicReference(initialFlashlightState)

        private val lensFacingRef = AtomicReference(initialLensFacing)

        private val cameraProviderRef = AtomicReference<ProcessCameraProvider?>(null)

        private val cameraRef = AtomicReference<Camera?>(null)

        private val cameraControlRef = AtomicReference<CameraControl?>(null)

        private val imageCaptureRef = AtomicReference<ImageCapture?>(null)

        override val previewView by lazy {
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )

                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

        private val _cameraState = MutableStateFlow<CameraState>(CameraState.Inactive)

        override val cameraState = _cameraState.asStateFlow()

        init {
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main.immediate) {
                try {
                    val provider = suspendCoroutine<ProcessCameraProvider> { continuation ->
                        ProcessCameraProvider.getInstance(context).also { future ->
                            future.addListener({
                                try {
                                    continuation.resume(future.get())
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                }
                            }, executor)
                        }
                    }

                    cameraProviderRef.set(provider)

                    bindCameraUseCases(flashlightStateRef.get(), lensFacingRef.get(), provider)
                } catch (e: Exception) {
                    _cameraState.value = CameraState.Error(e)
                }
            }
        }

        private suspend fun bindCameraUseCases(
            flashlightState: FlashlightState,
            lensFacing: LensFacing,
            cameraProvider: ProcessCameraProvider
        ) =
            withContext(Dispatchers.Main.immediate) {
                try {
                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }

                    val imageCapture =
                        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()

                    imageCaptureRef.set(imageCapture)

                    val cameraSelector = when (lensFacing) {
                        LensFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA

                        LensFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider.unbindAll()

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )

                    cameraRef.set(camera)

                    cameraControlRef.set(camera.cameraControl)

                    camera.cameraControl.enableTorch(flashlightState == FlashlightState.ENABLED)

                    flashlightStateRef.set(flashlightState)

                    lensFacingRef.set(lensFacing)

                    _cameraState.value = CameraState.Active(
                        flashlightState = flashlightState,
                        lensFacing = lensFacing
                    )
                } catch (e: Exception) {
                    _cameraState.value = CameraState.Error(e)
                }
            }

        override suspend fun setFlashlightState(flashlightState: FlashlightState) = withContext(Dispatchers.Main) {
            runCatching {
                val cameraControl = cameraControlRef.get() ?: error("Camera control not available")

                cameraControl.enableTorch(flashlightState == FlashlightState.ENABLED)

                flashlightStateRef.set(flashlightState)

                (_cameraState.value as? CameraState.Active)?.let {
                    _cameraState.value = it.copy(flashlightState = flashlightState)
                }

                flashlightStateRef.get()
            }
        }

        override suspend fun setLensFacing(lensFacing: LensFacing) = withContext(Dispatchers.Main) {
            runCatching {
                val cameraProvider = cameraProviderRef.get() ?: error("Camera provider not initialized")

                bindCameraUseCases(flashlightStateRef.get(), lensFacing, cameraProvider)

                (_cameraState.value as? CameraState.Active)?.let {
                    _cameraState.value = it.copy(lensFacing = lensFacing)
                }

                lensFacingRef.get()
            }
        }

        override suspend fun takePicture() = withContext(Dispatchers.IO) {
            runCatching {
                val imageCapture = imageCaptureRef.get() ?: error("ImageCapture not initialized")

                suspendCoroutine { continuation ->
                    imageCapture.takePicture(
                        cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                try {
                                    val bytes = ByteArrayOutputStream().use { baos ->
                                        val bitmap = image.toBitmap()

                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                                        bitmap.recycle()

                                        baos.toByteArray()
                                    }

                                    val picture = CameraPicture(bytes, image.width, image.height)

                                    continuation.resume(picture)
                                } catch (e: Exception) {
                                    continuation.resumeWithException(e)
                                } finally {
                                    image.close()
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                continuation.resumeWithException(exception)
                            }
                        })
                }
            }
        }

        override suspend fun close() = withContext(Dispatchers.Main) {
            runCatching {
                cameraProviderRef.get()?.unbindAll()

                cameraProviderRef.set(null)

                cameraRef.set(null)

                cameraControlRef.set(null)

                imageCaptureRef.set(null)

                cameraExecutor.shutdown()

                _cameraState.value = CameraState.Inactive
            }
        }
    }
}