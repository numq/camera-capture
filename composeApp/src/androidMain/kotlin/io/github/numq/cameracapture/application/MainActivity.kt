package io.github.numq.cameracapture.application

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.github.numq.cameracapture.di.androidModule
import io.github.numq.cameracapture.di.commonModule
import io.github.numq.cameracapture.permission.RequestPermission
import io.github.numq.cameracapture.server.StartServer
import io.github.numq.cameracapture.server.StopServer
import io.github.numq.cameracapture.settings.GetSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity(), AndroidScopeComponent {
    override val scope by activityScope()

    private val getSettings by lazy { scope.get<GetSettings>() }

    private val startServer by lazy { scope.get<StartServer>() }

    private val stopServer by lazy { scope.get<StopServer>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)

            modules(commonModule + androidModule + module { single<LifecycleOwner> { this@MainActivity } })
        }

        setContent {
            val multiplePermissionsState = rememberMultiplePermissionsState(
                listOf(
                    Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
                )
            )

            val hasPermissions = remember(multiplePermissionsState) {
                multiplePermissionsState.permissions.all { permissionState ->
                    ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }

            when {
                hasPermissions || multiplePermissionsState.allPermissionsGranted -> Application()

                else -> RequestPermission(
                    multiplePermissionsState = multiplePermissionsState, onRequestPermission = {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    })
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                getSettings
            }.execute(Unit).mapCatching { settings ->
                startServer.execute(StartServer.Input(host = settings.host, port = settings.port)).getOrThrow()
            }.onFailure { throwable ->
                Log.e("MainActivity", "Failed to start server", throwable)
            }.getOrDefault(Unit)
        }
    }

    override fun onPause() {
        lifecycleScope.launch(Dispatchers.Default) {
            withContext(NonCancellable) {
                stopServer.execute(Unit).onFailure { throwable ->
                    Log.e("MainActivity", "Failed to stop server", throwable)
                }.getOrDefault(Unit)
            }
        }

        super.onPause()
    }
}