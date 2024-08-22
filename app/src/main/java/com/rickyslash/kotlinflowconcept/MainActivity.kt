package com.rickyslash.kotlinflowconcept

import android.os.Bundle
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rickyslash.kotlinflowconcept.ui.theme.KotlinFlowConceptTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KotlinFlowConceptTheme {
                val counter = viewModel.counterStateFlow.collectAsState(0)
                val onIncrement = { viewModel.incrementCounter() }

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