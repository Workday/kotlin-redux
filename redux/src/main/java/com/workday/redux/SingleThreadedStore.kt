package com.workday.redux

open class SingleThreadedStore<S : State, A : Action>(
        private var state: S,
        private val reducer: Reducer<S, A>,
        private val middleware: List<Middleware<S, A>> = emptyList()
) : Store<S, A> {

    internal val subscriptions = mutableListOf<Subscription<S, A>>()

    override fun subscribe(subscription: Subscription<S, A>): Unsubscribe {
        subscriptions.add(subscription)
        subscription(state, ::dispatch)
        return {
            subscriptions.remove(subscription)
            if (subscriptions.isEmpty()) {
                middleware.forEach { it.dispose() }
            }
        }
    }

    private fun dispatch(action: A) {
        val newAction: A = applyMiddleware(state, action)
        val newState: S = applyReducer(state, newAction)

        if (state != newState) {
            state = newState
            subscriptions.forEach { it(state, ::dispatch) }
        }
    }

    private fun applyMiddleware(state: S, action: A): A {
        return next(0)(state, action, ::dispatch)
    }

    private fun next(index: Int): Next<S, A> {
        if (index == middleware.size) {
            return { _, action, _ -> action }
        }

        return { state, action, dispatch ->
            middleware[index].invoke(state,
                                     action,
                                     dispatch,
                                     next(index + 1))
        }
    }

    private fun applyReducer(currentState: S, action: A): S {
        return reducer(currentState, action)
    }
}
