package com.rickyslash.kotlinflowconcept

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
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

    private val forFlatMapFlow = flow {
        emit(1)
        delay(250L)
        emit(2)
    }

    init {
        viewModelScope.launch {
            collectBasicFlow()
            collectSimpleOperator()
            collectTerminalOperator()
            collectFlatteningOperator()
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

    // 2.2 Terminal Flow Operator: Last operator to be operated on Flow collection
    // - count  -> count number of Flow items after operated
    // - reduce -> do operation on Flow items & accumulate the result
    // - fold   -> reduce with initial value
    private suspend fun collectTerminalOperator() {
        val countResult = countdownFlow
            .count { it % 2 == 0 }

        Log.d(TAG, "2.2.1 Count of filtered Flow: $countResult")

        val reduceResult = countdownFlow
            .filter { it % 2 == 1 }
            .reduce{ acc, item -> acc + item }

        Log.d(TAG, "2.2.2 Accumulated sum of odd Flow: $reduceResult")

        val foldResult = countdownFlow
            .filter { it % 2 == 1 }
            .fold(100) { acc, item -> acc + item }

        Log.d(TAG, "2.2.3 Accumulated sum of odd Flow by initial of 100: $foldResult")
    }

    // 3.1 Flattening Flow Operator: Emit some data on Flow based on another Flow
    // - flatMapConcat: Will pass emission of the first Flow 1 by 1 to be processed by another flow
    // - flatMapMerge: Will pass emission of the first Flow to another flow as soon as it's available
    // - flatMapLatest: When Flow emits new value, the previous Flow process is cancelled & replaced
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun collectFlatteningOperator() {
        forFlatMapFlow
            .flatMapConcat { value ->
                flow {
                    emit(value + 1)
                    delay(250L)
                    emit(value + 2)
                }
            }
            .collect { Log.d(TAG, "3.1 flatMapConcat: $it") }

        forFlatMapFlow
            .flatMapMerge { value ->
                flow {
                    emit(value + 1)
                    delay(250L)
                    emit(value + 2)
                }
            }
            .collect { Log.d(TAG, "3.2 flatMapMerge: $it") }

        forFlatMapFlow
            .flatMapLatest { value ->
                flow {
                    emit(value + 1)
                    delay(250L)
                    emit(value + 2)
                }
            }
            .collect { Log.d(TAG, "3.3 flatMapLatest: $it") }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}