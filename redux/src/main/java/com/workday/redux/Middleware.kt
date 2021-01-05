package com.workday.redux

typealias Next<State, Action> = (state: State, action: Action, dispatch: Dispatch<Action>) -> Action

interface Middleware<State, Action> {
    fun invoke(state: State, action: Action, dispatch: Dispatch<Action>, next: Next<State, Action>): Action
    fun dispose() {}
}
