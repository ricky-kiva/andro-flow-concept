package com.rickyslash.kotlinflowconcept

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.rickyslash.kotlinflowconcept.helper.TestDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var testDispatchers: TestDispatchers

    @Before
    fun setUp() {
        testDispatchers = TestDispatchers()
        viewModel = MainViewModel(testDispatchers)
    }

    // Test basic Flow with time being advanced
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `countdownFlow, properly counts down from 5 to 0`() = runBlocking {
        val testScope = TestScope(testDispatchers.main)

        viewModel.countdownFlow.test {
            for (i in 5 downTo 0) {
                testScope.advanceTimeBy(viewModel.countdownDelay)
                val emission = awaitItem()
                assertThat(emission).isEqualTo(i)
            }

            cancelAndConsumeRemainingEvents()
        }
    }

    // Test SharedFLow emission is successfully received
    @Test
    fun `triggerSharedFlow, return correct string`() = runBlocking {
        val testJob = launch { // uses Job so `triggerSharedFlow` & `testJob` runs simultaneously
            viewModel.hotSharedFlow.test {
                val emission = awaitItem()
                assertThat(emission).isEqualTo("HOT! THE FLOW IS SO HOT!")
            }
        }

        viewModel.triggerSharedFlow()
        testJob.join()
        testJob.cancel()
    }
}