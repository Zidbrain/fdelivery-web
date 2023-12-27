package io.github.zidbrain.fdelivery.viewmodel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.experimental.ExperimentalTypeInference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class ViewModel<Action : Any, State, Event> {
    protected val viewModelScope = CoroutineScope(Dispatchers.Default)

    abstract val initialState: State
    abstract val actions: Map<KClass<out Action>, UseCase<Action, State, Event>>

    private val _state by lazy { MutableStateFlow(initialState) }
    val state: StateFlow<State> by lazy { _state }
    val currentState
        get() = state.value

    private val _events = MutableSharedFlow<Event>()
    val events: Flow<Event> = _events

    private val sendJobs = mutableMapOf<KClass<out Action>, Job>()

    inline fun <reified Actual : State> State.ensure() =
        ReadOnlyProperty<Any?, Actual> { _, _ -> this as Actual }

    inline fun <reified Actual : State> State.convert() =
        ReadOnlyProperty<Any?, Actual?> { _, _ -> this as? Actual }

    open fun onActionsBound() {}

    fun sendAction(action: Action) {
        val useCase = actions[action::class]
        sendJobs[action::class]?.cancel()
        sendJobs[action::class] = viewModelScope.launch {
            _state.emitAll(useCase!!.process(action))
        }
    }

    fun sendEvent(event: Event) = viewModelScope.launch {
        _events.emit(event)
    }

    fun close() {
        viewModelScope.cancel()
    }

    @OptIn(ExperimentalTypeInference::class)
    fun <Actual : Action> SimpleUseCase(@BuilderInference block: suspend UseCaseCollector<in Actual, State, Event>.(Actual) -> Unit): UseCase<Action, State, Event> =
        UseCase(this, block as suspend UseCaseCollector<in Actual, State, Event>.(Action) -> Unit)
}

class UseCaseCollector<Action : Any, State, Event> internal constructor(
    @PublishedApi
    internal val viewModel: ViewModel<Action, State, Event>,
    @PublishedApi
    internal val collector: FlowCollector<State>
) : FlowCollector<State> by collector {

    suspend inline fun <reified Actual : State> emitAs(copy: (Actual) -> Actual) {
        val actual = viewModel.currentState as Actual
        emit(copy(actual))
    }
}

@OptIn(ExperimentalTypeInference::class)
class UseCase<out Action : Any, out State, Event>(
    private val viewModel: ViewModel<Action, State, Event>,
    @BuilderInference private val block: suspend UseCaseCollector<Action, State, Event>.(Action) -> Unit
) {
    private var catch: (@BuilderInference suspend FlowCollector<State>.(Throwable) -> Unit)? = null

    fun process(action: @UnsafeVariance Action): Flow<State> = flow {
        val collector = UseCaseCollector(viewModel, this)
        collector.block(action)
    }.let { if (catch != null) it.catch(catch!!) else it }

    fun catch(@BuilderInference block: suspend FlowCollector<@UnsafeVariance State>.(Throwable) -> Unit): UseCase<Action, State, Event> =
        apply {
            catch = block
        }
}