package io.github.numq.cameracapture.interaction

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.numq.cameracapture.camera.CameraPreview
import io.github.numq.cameracapture.camera.CameraState
import io.github.numq.cameracapture.camera.FlashlightState
import io.github.numq.cameracapture.server.ServerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionView(feature: InteractionFeature, onError: (Exception) -> Unit) {
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }

    val state by feature.state.collectAsState()

    val event by feature.events.filterIsInstance<InteractionEvent.Error>().collectAsState(null)

    LaunchedEffect(event) {
        event?.exception?.let(onError)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            this@Column.AnimatedVisibility(
                visible = state.cameraState is CameraState.Error, enter = fadeIn(), exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        space = 8.dp, alignment = Alignment.CenterVertically
                    )
                ) {
                    Text(
                        "Error", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        (state.cameraState as CameraState.Error).exception.localizedMessage
                            ?: "Unknown camera exception",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            this@Column.AnimatedVisibility(
                visible = state.cameraState is CameraState.Inactive, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }
            this@Column.AnimatedVisibility(
                visible = state.cameraState is CameraState.Active, enter = fadeIn(), exit = fadeOut()
            ) {
                (state.cameraState as? CameraState.Active)?.let { cameraState ->
                    Scaffold(
                        modifier = Modifier.fillMaxSize(), floatingActionButton = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 8.dp, alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                FloatingActionButton(onClick = {
                                    coroutineScope.launch {
                                        feature.execute(InteractionCommand.Camera.ToggleFlashlight)
                                    }
                                }, shape = CircleShape) {
                                    when (cameraState.flashlightState) {
                                        FlashlightState.ENABLED -> Icon(
                                            Icons.Default.FlashlightOff, null
                                        )

                                        FlashlightState.DISABLED -> Icon(
                                            Icons.Default.FlashlightOn, null
                                        )
                                    }
                                }
                                FloatingActionButton(onClick = {
                                    coroutineScope.launch {
                                        feature.execute(InteractionCommand.Camera.Switch)
                                    }
                                }, shape = CircleShape) {
                                    Icon(Icons.Default.Cameraswitch, null)
                                }
                            }
                        }, floatingActionButtonPosition = FabPosition.Center
                    ) { paddingValues ->
                        CameraPreview(modifier = Modifier.fillMaxSize().padding(paddingValues))
                    }
                }
            }
        }
        Box(
            modifier = Modifier.combinedClickable(
                enabled = state.serverState is ServerState.Error || state.serverState is ServerState.Connected || state.serverState is ServerState.Disconnected,
                onClick = {
                    coroutineScope.launch {
                        feature.execute(InteractionCommand.Server.Restart)
                    }
                }).fillMaxHeight(.1f).fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        feature.execute(InteractionCommand.Dialog.Open)
                    }
                }) {
                    Icon(Icons.Default.Settings, null)
                }
                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp, alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        Text(
                            state.serverState.name,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (state.serverState) {
                                is ServerState.Error -> MaterialTheme.colorScheme.error

                                is ServerState.Connected -> MaterialTheme.colorScheme.primary

                                is ServerState.Disconnected -> MaterialTheme.colorScheme.outline

                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        when (val serverState = state.serverState) {
                            is ServerState.Error -> Text(
                                serverState.exception.localizedMessage ?: "Unknown server exception",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )

                            is ServerState.Connected -> Text(
                                "${serverState.host}:${serverState.port}",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                            )

                            else -> Unit
                        }
                    }
                }
                when (state.serverState) {
                    is ServerState.Error -> Unit

                    else -> IconButton(onClick = {}, enabled = false) {
                        when (state.serverState) {
                            is ServerState.Disconnected -> Icon(Icons.Default.CloudOff, null)

                            is ServerState.Connected -> Icon(Icons.Default.Cloud, null)

                            is ServerState.Connecting, is ServerState.Disconnecting -> {
                                val infiniteTransition = rememberInfiniteTransition()

                                val angle by infiniteTransition.animateFloat(
                                    initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                                        animation = tween(2_000, easing = LinearEasing)
                                    )
                                )

                                Icon(
                                    Icons.Default.Sync, null, modifier = Modifier.rotate(angle)
                                )
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    if (state.isDialogVisible) {
        val (dialogPort, setDialogPort) = remember(state.port) { mutableIntStateOf(state.port) }

        Dialog(onDismissRequest = {
            coroutineScope.launch {
                coroutineScope.launch {
                    feature.execute(InteractionCommand.Dialog.Close(port = dialogPort))
                }
            }
        }) {
            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically)
                ) {
                    Text("Change port", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp, alignment = Alignment.CenterHorizontally
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                setDialogPort((dialogPort - 1).coerceAtLeast(0))
                            }, enabled = dialogPort > 0) {
                                Icon(Icons.AutoMirrored.Filled.ArrowLeft, null)
                            }
                        }
                        OutlinedTextField(
                            value = "$dialogPort",
                            onValueChange = { value ->
                                value.filter { it in '0'..'9' }.toIntOrNull()?.let(setDialogPort)
                            },
                            modifier = Modifier.fillMaxWidth(.5f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            placeholder = {
                                Text("8090", style = MaterialTheme.typography.bodySmall)
                            },
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                setDialogPort((dialogPort + 1).coerceAtMost(65535))
                            }, enabled = dialogPort < 65535) {
                                Icon(Icons.AutoMirrored.Filled.ArrowRight, null)
                            }
                        }
                    }
                }
            }
        }
    }
}