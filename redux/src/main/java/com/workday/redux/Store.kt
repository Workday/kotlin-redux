package com.workday.redux

interface Store<S : State, A: Action> {
    fun subscribe(subscription: Subscription<S, A>): Unsubscribe
}
