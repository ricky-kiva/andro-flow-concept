package com.rickyslash.kotlinflowconcept

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rickyslash.kotlinflowconcept.ui.theme.KotlinFlowConceptTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KotlinFlowConceptTheme {
                val counter = viewModel.counterStateFlow.collectAsState(0)
                val onIncrement = { viewModel.incrementCounter() }

                // send SharedFlow data to UI
                LaunchedEffect(true) { // `true` to launch LaunchEffect only once
                    viewModel.helloSharedFlow.collect {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }

                viewModel.sayHelloToTheFlow()

                Scaffold { innerPadding ->
                    viewModel.apply {
                        MainContent(
                            counter = counter.value,
                            onIncrement = onIncrement,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }

                }
            }
        }
    }
}

@Composable
private fun MainContent(
    counter: Int,
    onIncrement: () -> Unit,
    modifier: Modifier
) {
    Surface {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = onIncrement) {
                Text(
                    text = stringResource(R.string.label_counter)
                        .format(counter)
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KotlinFlowConceptTheme {
        Greeting("Android")
    }
}

// Function to get StateFlow on View-based activity
fun <T> ComponentActivity.collectLatestLifeCycleFlow(
    flow: Flow<T>,
    collect: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest(collect)
        }
    }
}

// Function to get SharedFlow on View-based activity
fun <T> ComponentActivity.collectLifeCycleFlow(
    flow: Flow<T>,
    collect: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collect)
        }
    }
}
