package com.workday.kotlinredux

import com.workday.redux.Reducer

class MainReducer : Reducer<MainState, CounterAction> {
    override fun invoke(currentState: MainState, newAction: CounterAction): MainState {
        return currentState.copy(count = countReducer(currentState, newAction))
    }

    private fun countReducer(currentState: MainState, newAction: CounterAction): Int {
        return when (newAction) {
            is CounterAction.IncrementAction -> currentState.count + 1
            is CounterAction.DecrementAction -> currentState.count - 1
        }
    }
}
