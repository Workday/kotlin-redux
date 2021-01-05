package com.workday.redux

typealias Reducer<State, Action> = (currentState: State, newAction: Action) -> State
