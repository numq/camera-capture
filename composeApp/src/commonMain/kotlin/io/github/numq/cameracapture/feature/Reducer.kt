package io.github.numq.cameracapture.feature

interface Reducer<in Command, State, Event> {
    suspend fun reduce(state: State, command: Command): Transition<State, Event>

    fun transition(state: State, vararg event: Event) = Transition(state = state, events = event.asList())
}