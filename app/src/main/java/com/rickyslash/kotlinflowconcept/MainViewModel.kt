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
        collectBasicFlow()
    }

    // 1. Basic Flow: Collect
    private fun collectBasicFlow() {
       viewModelScope.launch {
           countdownFlow.collect { time ->
               Log.d(TAG, "1. Flow: $time")
           }
       }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}