package com.rickyslash.kotlinflowconcept

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickyslash.kotlinflowconcept.helper.DefaultDispatchers
import com.rickyslash.kotlinflowconcept.helper.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.launch

class MainViewModel(
    private val dispatchers: DispatcherProvider = DefaultDispatchers()
): ViewModel() {

    val countdownDelay = 250L

    // 1. Basic Flow: Data type
    val countdownFlow = flow {
        val startValue = 5
        var currentValue = startValue

        emit(currentValue)

        while (currentValue > 0) {
            delay(countdownDelay)
            currentValue--

            emit(currentValue)
        }
    }.flowOn(dispatchers.main)

    private val forFlatMapFlow = flow {
        emit(1)
        delay(250L)
        emit(2)
    }.flowOn(dispatchers.main)

    private val mealCoursesFlow = flow {
        delay(50L)
        emit("Appetizer")
        delay(200L)
        emit("Main Dish")
        delay(20L)
        emit("Dessert")
    }.flowOn(dispatchers.main)

    // 5. StateFlow: A Flow that holds single updatable value, that emits updates to collectors
    private val _counterStateFlow = MutableStateFlow(0)
    val counterStateFlow = _counterStateFlow.asStateFlow()

    // 6. SharedFlow: Hot state-sharing Flow that allows multiple consumers collect emitted values
    private val _hotSharedFlow = MutableSharedFlow<String>()
    val hotSharedFlow = _hotSharedFlow.asSharedFlow()

    // 6.1 SharedFlow: Display on UI
    private val _helloSharedFlow = MutableSharedFlow<String>()
    val helloSharedFlow = _helloSharedFlow.asSharedFlow()

    // 5. StateFlow: Function to modify StateFlow value
    fun incrementCounter() {
        _counterStateFlow.value += 1
    }

    // 6. Function to trigger SharedFlow
    fun triggerSharedFlow() {
        viewModelScope.launch(dispatchers.main) {
            val hot = StringBuilder()
                .append("HOT! ")
                .append("THE FLOW IS SO HOT!")

            _hotSharedFlow.emit(hot.toString())
        }
    }

    // 6.1 SharedFlow: Function to emit Flow
    fun sayHelloToTheFlow() {
        viewModelScope.launch(dispatchers.main) {
            _helloSharedFlow.apply {
                emit("Good Morning!")
                delay(500L)
                emit("Guten Morgen!")
                delay(500L)
                emit("Ohayo Gozaimasu!")
                delay(500L)
                emit("Sabah Alkhayr!")
                delay(500L)
                emit("Selamat Pagi!")
            }
        }
    }

    init {
        collectTwiceSharedFlow()

        viewModelScope.launch(dispatchers.main) {
            collectBasicFlow()
            collectSimpleOperator()
            collectTerminalOperator()
            collectFlatteningOperator()
            collectEmissionHandlingOperator()
        }

        triggerSharedFlow()
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

    // 3.4 Emission Handling Operators: Handle the flow of emission when collected
    // - Buffer: Collect Flow value as soon as it is emitted
    // - Conflate: Put simply, only collect & process the first & last emission
    // - collectLatest: Cancels previous collector and only processes the most recent value emitted
    private suspend fun collectEmissionHandlingOperator() {
        mealCoursesFlow
            .onEach { Log.d(TAG, "3.4.1.1 $it: is delivered!") }
            .buffer()
            .collect {
                Log.d(TAG, "3.4.1.2 $it: Now eating!")
                delay(300L)
                Log.d(TAG, "3.4.1.3 $it: Finished eating!")
            }

        mealCoursesFlow
            .onEach { Log.d(TAG, "3.4.2.1 $it: is delivered!") }
            .conflate()
            .collect {
                Log.d(TAG, "3.4.2.2 $it: Now eating!")
                delay(300L)
                Log.d(TAG, "3.4.2.3 $it: Finished eating!")
            }

        mealCoursesFlow
            .onEach { Log.d(TAG, "3.4.3.1 $it: is delivered!") }
            .collectLatest {
                Log.d(TAG, "3.4.3.2 $it: Now eating!")
                delay(300L)
                Log.d(TAG, "3.4.3.3 $it: Finished eating!")
            }
    }

    // 6. Function to collect SharedFlow
    private fun collectTwiceSharedFlow() {
        viewModelScope.launch(dispatchers.main) {
            hotSharedFlow.collect {
                delay(200L)
                Log.d(TAG, "6.1 First SharedFlow: $it")
            }
        }

        viewModelScope.launch(dispatchers.main) {
            hotSharedFlow.collect {
                delay(300L)
                Log.d(TAG, "6.2 First SharedFlow: $it")
            }
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}