package com.rickyslash.kotlinflowconcept

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    // 1. Basic Flow: Data type
    private val countdownFlow = flow {
        val startValue = 10
        var currentValue = startValue

        emit(currentValue)

        while (currentValue > 0) {
            delay(250L)
            currentValue--

            emit(currentValue)
        }
    }

    init {
        viewModelScope.launch {
            collectBasicFlow()
            collectSimpleOperator()
        }
    }

    // 1. Basic Flow: Collect
    private suspend fun collectBasicFlow() {
       countdownFlow.collect { time ->
           Log.d(TAG, "1. Flow: $time")
       }
    }

    // 2.1. Simple Flow Operator: Do data processing on Flow items
    // - filter -> filter each flow item based on boolean
    // - map    -> transform each flow item
    // - onEach -> do non-transforming operation on each flow item
    // - count  -> reducer to count amount of item that is true
    private suspend fun collectSimpleOperator() {
        countdownFlow
            .filter { time -> time % 2 == 0 }
            .map { time -> time * time }
            .onEach { time -> Log.d(TAG, "2.1.A Logged on each: $time") }
            .collect { time -> Log.d(TAG, "2.1.B Augmented Flow: $time") }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}