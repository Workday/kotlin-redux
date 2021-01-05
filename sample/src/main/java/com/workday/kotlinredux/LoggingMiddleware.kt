package com.workday.kotlinredux

import android.util.Log
import com.workday.redux.Dispatch
import com.workday.redux.Middleware
import com.workday.redux.Next

class LoggingMiddleware : Middleware<MainState, CounterAction> {
    override fun invoke(
        state: MainState,
        action: CounterAction,
        dispatch: Dispatch<CounterAction>,
        next: Next<MainState, CounterAction>
    ): CounterAction {
        Log.d("Redux Sample App Log", "Action: $action")
        return next(state, action, dispatch)
    }
}
