package com.workday.redux

sealed class MockAction : Action {
    object Update1Action : MockAction()
    object Update2Action : MockAction()
    object NoUpdateAction : MockAction()
    object MiddlewareCalledAction : MockAction()
}

data class MockState(
        val updated1: Boolean = false,
        val updated2: Boolean = false,
        val middlewareCalled: Boolean = false
) : State

class MockReducer : Reducer<MockState, MockAction> {
    override fun invoke(state: MockState, action: MockAction): MockState {
        return when (action) {
            is MockAction.Update1Action -> state.copy(updated1 = true)
            is MockAction.Update2Action -> state.copy(updated2 = true)
            is MockAction.NoUpdateAction -> state
            is MockAction.MiddlewareCalledAction -> state.copy(middlewareCalled = true)
        }
    }
}

class MockDispatchingMiddleware(private val disposable: Disposable) : Middleware<MockState, MockAction> {
    override fun invoke(
            state: MockState,
            action: MockAction,
            dispatch: Dispatch<MockAction>,
            next: Next<MockState, MockAction>
    ): MockAction {
        if (action != MockAction.MiddlewareCalledAction)
            dispatch(MockAction.MiddlewareCalledAction)

        return action
    }

    override fun dispose() {
        disposable.dispose()
    }
}

interface Disposable {
    fun dispose()
}
