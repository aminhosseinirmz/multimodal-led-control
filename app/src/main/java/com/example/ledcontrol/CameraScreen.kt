package com.example.ledcontrol
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
//GestureRecognizerHelper	Handles MediaPipe gesture detection
//PreviewView	Shows live camera feed
//setupCamera(...)	Connects the camera to gesture recognition
//onResults(...)	Sends results to LedViewModel
//permissionLauncher	Asks for camera permission
@Composable
fun CameraScreen(
//    gestureRecognizerHelper: GestureRecognizerHelper,
    ledViewModel: LedViewModel
) {
    val context = LocalContext.current

    val gestureRecognizerHelper = remember {
        GestureRecognizerHelper(
            context = context,
            runningMode = RunningMode.LIVE_STREAM
        )
    }
    Log.d("CameraScreen", "CameraScreen: Composable started")
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to track if camera permission is granted.
    var hasCameraPermission by remember { mutableStateOf(false) }

    // State to hold gesture recognition results
    var gestureResults by remember { mutableStateOf<List<GestureRecognizerHelper.ResultBundle>>(emptyList()) }
    var lastResult by remember { mutableStateOf<GestureRecognizerHelper.ResultBundle?>(null) }

    // Initialize GestureRecognizerHelper with a listener to update the state
    LaunchedEffect(gestureRecognizerHelper) {
        Log.d("CameraScreen", "LaunchedEffect(gestureRecognizerHelper): Started")
        gestureRecognizerHelper.gestureRecognizerListener =
            object : GestureRecognizerHelper.GestureRecognizerListener {
                override fun onError(error: String, errorCode: Int) {
                    Log.e("CameraScreen", "Gesture recognition error: $error")
                }

                override fun onResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
                    Log.d("CameraScreen", "onResults: Callback triggered")
                    // Update the state with the new results
                    if (lastResult?.results?.firstOrNull()?.landmarks() != resultBundle.results.firstOrNull()?.landmarks()) {
                        gestureResults = listOf(resultBundle)
                        lastResult = resultBundle
                        ledViewModel.updateGestureResults(resultBundle)
                    }
                }
            }
    }

    val cameraFacing = CameraSelector.LENS_FACING_FRONT
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("CameraScreen", "permissionLauncher: Callback triggered, isGranted = $isGranted")
        hasCameraPermission = isGranted
    }

    // Check permissions and request if needed
    LaunchedEffect(Unit) {
        Log.d("CameraScreen", "LaunchedEffect(Unit): Started")
        val permissionCheckResult =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            Log.d("CameraScreen", "LaunchedEffect(Unit): Permission already granted")
            hasCameraPermission = true
        } else {
            Log.d("CameraScreen", "LaunchedEffect(Unit): Requesting permission")
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Dispose the background executor when the composable is disposed
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            Log.d("CameraScreen", "onDispose: Called")
            backgroundExecutor.shutdown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Log.d("CameraScreen", "Column: Composable started, hasCameraPermission = $hasCameraPermission")
        // Only show the camera preview if we have permission
        if (hasCameraPermission) {
            Log.d("CameraScreen", "AndroidView: Creating AndroidView")
            AndroidView(
                modifier = Modifier.weight(1f), // Take up most of the screen
                factory = { context ->
                    Log.d("CameraScreen", "AndroidView: factory lambda called")
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = { previewView ->
                    Log.d("CameraScreen", "AndroidView: update lambda called, hasCameraPermission = $hasCameraPermission")
                    // Set up the camera when the PreviewView is ready
                    setupCamera(
                        context,
                        lifecycleOwner,
                        previewView,
                        cameraFacing,
                        backgroundExecutor,
                        gestureRecognizerHelper
                    )
                }
            )
        }
    }
}
@Composable
fun GestureResults(results: List<GestureRecognizerHelper.ResultBundle>) {
    Log.d("GestureResults", "GestureResults: Composable started")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        results.forEach { resultBundle ->
            resultBundle.results.forEach { result ->
                Text(text = "Gesture: ${result.gestures().firstOrNull()?.get(0)?.categoryName() ?: "Unknown"}")
                Text(text = "Inference Time: ${resultBundle.inferenceTime} ms")
            }
        }
    }
}
/**
 * Sets up the camera for live stream analysis.
 *
 * @param context The application context.
 * @param lifecycleOwner The lifecycle owner for binding the camera.
 * @param previewView The PreviewView to display the camera feed.
 * @param cameraFacing The desired camera facing direction (e.g., CameraSelector.LENS_FACING_BACK).
 * @param backgroundExecutor The executor for background tasks.
 * @param gestureRecognizerHelper The helper for gesture recognition.
 */
private fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraFacing: Int,
    backgroundExecutor: ExecutorService,
    gestureRecognizerHelper: GestureRecognizerHelper
) {
    Log.d("CameraScreen", "setupCamera: Function called")
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        Log.d("CameraScreen", "setupCamera: cameraProviderFuture.addListener - Listener triggered")
        // Used a more descriptive name
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

        // Use a constant for the aspect ratio
        val targetAspectRatio = AspectRatio.RATIO_4_3

        // Build the camera selector
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cameraFacing)
            .build()
        Log.d("CameraScreen", "setupCamera: cameraSelector created")

        // Build the preview use case
        val preview = Preview.Builder()
            .setTargetAspectRatio(targetAspectRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        Log.d("CameraScreen", "setupCamera: preview created")

        // Build the image analysis use case
        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(targetAspectRatio)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(backgroundExecutor) { image ->
                    gestureRecognizerHelper.recognizeLiveStream(image)
                }
            }
        Log.d("CameraScreen", "setupCamera: imageAnalyzer created")

        // Unbind any existing use cases
        cameraProvider.unbindAll()
        Log.d("CameraScreen", "setupCamera: cameraProvider.unbindAll() called")

        try {
            // Bind the use cases to the lifecycle
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalyzer
            )
            Log.d("CameraScreen", "setupCamera: cameraProvider.bindToLifecycle() called")
            Log.d("CameraScreen", "Camera setup successful")
        } catch (exc: Exception) {
            Log.e("CameraScreen", "Camera setup failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}