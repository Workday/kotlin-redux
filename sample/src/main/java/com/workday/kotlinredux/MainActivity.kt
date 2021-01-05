package com.workday.kotlinredux

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.workday.redux.SingleThreadedStore
import com.workday.redux.Unsubscribe
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var storeUnsubscriber: Unsubscribe
    private lateinit var store: SingleThreadedStore<MainState, CounterAction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = SingleThreadedStore(
                state = MainState(count = 0),
                reducer = MainReducer(),
                middleware = listOf(LoggingMiddleware())
        )
    }

    override fun onResume() {
        super.onResume()

        storeUnsubscriber = store.subscribe { currentState, dispatch ->
            counter.text = "Count: ${currentState.count}"

            incrementButton.setOnClickListener {
                dispatch(CounterAction.IncrementAction)
            }

            decrementButton.setOnClickListener {
                dispatch(CounterAction.DecrementAction)
            }
        }
    }

    override fun onPause() {
        storeUnsubscriber.invoke()

        super.onPause()
    }
}