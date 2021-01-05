package com.workday.redux

typealias Subscription<State, Action> = (currentState: State, dispatch: Dispatch<Action>) -> Unit

typealias Unsubscribe = () -> Unit
