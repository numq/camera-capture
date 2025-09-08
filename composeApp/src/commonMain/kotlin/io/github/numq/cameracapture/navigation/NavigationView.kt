package io.github.numq.cameracapture.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.numq.cameracapture.interaction.InteractionFeature
import io.github.numq.cameracapture.interaction.InteractionView
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationView(feature: NavigationFeature = koinInject()) {
    val state by feature.state.collectAsState()

    val error by feature.events.filterIsInstance<NavigationEvent.Error>().collectAsState(null)

    val exceptions = remember { mutableStateListOf<Exception?>() }

    LaunchedEffect(error) {
        error?.exception?.let(exceptions::add)
    }

    when (val state = state) {
        is NavigationState.Initialization -> Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is NavigationState.Interaction -> {
            val interactionFeature = koinInject<InteractionFeature> {
                parametersOf(state.initialSettings)
            }

            InteractionView(feature = interactionFeature, onError = exceptions::add)
        }

        is NavigationState.Error -> Column(
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
                state.exception.localizedMessage ?: "Unknown initialization exception",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    exceptions.firstOrNull()?.let { exception ->
        BasicAlertDialog(onDismissRequest = { exceptions.removeFirstOrNull() }, content = {
            Surface(
                modifier = Modifier.fillMaxWidth(.75f).fillMaxHeight(.5f), shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        space = 8.dp, alignment = Alignment.CenterVertically
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp, alignment = Alignment.CenterHorizontally
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("An error occurred", style = MaterialTheme.typography.labelLarge)

                        Icon(Icons.Default.Error, null)
                    }

                    Text(
                        exception.message ?: "Unknown error", style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        })
    }
}