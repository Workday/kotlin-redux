package com.workday.redux

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SingleThreadedStoreTest {

    private val initialState = MockState()
    private val reducer = MockReducer()
    private val disposable = mock<Disposable>()
    private val dispatchingMiddleware = MockDispatchingMiddleware(disposable)

    private lateinit var store: SingleThreadedStore<MockState, MockAction>

    @Before fun setUp() {
        store = SingleThreadedStore(state = initialState,
                            reducer = reducer,
                            middleware = listOf(dispatchingMiddleware))
    }

    @Test fun `WHEN store initialized THEN no subscriptions`() {
        assertEquals(0, store.subscriptions.size)
    }

    @Test fun `GIVEN store initialized WHEN store subscribed to THEN returns unsubscribe lambda`() {
        val unsubscribe = store.subscribe { _, _ ->
            // do nothing
        }

        assertNotNull(unsubscribe)
    }

    @Test fun `GIVEN store initialized WHEN store subscribed to THEN correct number of subscriptions`() {
        assertEquals(0, store.subscriptions.size)

        store.subscribe { _, _ ->
            // do nothing
        }

        assertEquals(1, store.subscriptions.size)
    }

    @Test fun `GIVEN store initialized WHEN store subscribed to multiple times THEN correct number of subscriptions`() {
        assertEquals(0, store.subscriptions.size)

        repeat(3) {
            store.subscribe { _, _ ->
                // do nothing
            }
        }

        assertEquals(3, store.subscriptions.size)
    }

    @Test fun `GIVEN store subscribed to WHEN unsubscribe THEN correct number of subscriptions`() {
        val unsubscribe = store.subscribe { _, _ ->
            // do nothing
        }

        unsubscribe.invoke()

        assertEquals(0, store.subscriptions.size)
    }

    @Test fun `GIVEN two subscribers WHEN one unsubscribes THEN does not call dispose on middleware`() {
        val middleware1 = mock<Middleware<MockState, MockAction>>()
        val middleware2 = mock<Middleware<MockState, MockAction>>()
        val simpleStore = SingleThreadedStore(state = initialState,
                                      reducer = mock(),
                                      middleware = listOf(middleware1, middleware2))
        val unsubscribe1 = simpleStore.subscribe { _, _ ->
            // do nothing
        }
        simpleStore.subscribe { _, _ ->
            // do nothing
        }

        unsubscribe1.invoke()

        verify(middleware1, times(0)).dispose()
        verify(middleware2, times(0)).dispose()
    }

    @Test fun `GIVEN two subscribers WHEN last subscriber unsubscribes THEN calls dispose on all middleware`() {
        val middleware1 = mock<Middleware<MockState, MockAction>>()
        val middleware2 = mock<Middleware<MockState, MockAction>>()
        val simpleStore = SingleThreadedStore(state = initialState,
                                      reducer = mock(),
                                      middleware = listOf(middleware1, middleware2))
        val unsubscribe1 = simpleStore.subscribe { _, _ ->
            // do nothing
        }
        val unsubscribe2 = simpleStore.subscribe { _, _ ->
            // do nothing
        }

        unsubscribe1.invoke()
        unsubscribe2.invoke()

        verify(middleware1).dispose()
        verify(middleware2).dispose()
    }

    @Test fun `GIVEN store subscribed to WHEN dispatch update action THEN updates state`() {
        lateinit var dispatcher: Dispatch<MockAction>
        lateinit var newState: MockState
        store.subscribe { state, dispatch ->
            newState = state
            dispatcher = dispatch
        }
        assertFalse(newState.updated1)

        dispatcher.invoke(MockAction.Update1Action)

        assertTrue(newState.updated1)
    }

    @Test fun `GIVEN store subscribed to WHEN dispatch no update action THEN does not update state`() {
        lateinit var dispatcher: Dispatch<MockAction>
        lateinit var newState: MockState
        store.subscribe { state, dispatch ->
            newState = state
            dispatcher = dispatch
        }

        dispatcher.invoke(MockAction.NoUpdateAction)

        assertFalse(newState.updated1)
        assertFalse(newState.updated2)
    }

    @Test fun `GIVEN store subscribed to and unsubscribed to WHEN dispatch update action THEN state is not updated for this subscription`() {
        lateinit var dispatcher: Dispatch<MockAction>
        lateinit var newState: MockState
        val unsubscribe = store.subscribe { state, dispatch ->
            newState = state
            dispatcher = dispatch
        }
        unsubscribe.invoke()

        dispatcher.invoke(MockAction.Update1Action)

        assertFalse(newState.updated1)
    }

    @Test fun `GIVEN store subscribed to in multiple places WHEN dispatch multiple update actions THEN updates state correctly`() {
        lateinit var dispatcher1: Dispatch<MockAction>
        lateinit var dispatcher2: Dispatch<MockAction>
        lateinit var newState: MockState
        store.subscribe { _, dispatch ->
            dispatcher1 = dispatch
        }
        store.subscribe { state, dispatch ->
            newState = state
            dispatcher2 = dispatch
        }
        assertFalse(newState.updated1)
        assertFalse(newState.updated2)

        dispatcher1.invoke(MockAction.Update1Action)
        dispatcher2.invoke(MockAction.Update2Action)

        assertTrue(newState.updated1)
        assertTrue(newState.updated2)
    }

    @Test fun `GIVEN one subscription subscribed and one subscription unsubscribed WHEN dispatch update action from subscribed THEN state updates subscribed subscription`() {
        lateinit var dispatcher: Dispatch<MockAction>
        lateinit var newState1: MockState
        lateinit var newState2: MockState
        store.subscribe { state, dispatch ->
            newState1 = state
            dispatcher = dispatch
        }
        val unsubscribe = store.subscribe { state, _ ->
            newState2 = state
        }
        unsubscribe.invoke()
        assertFalse(newState1.updated1)
        assertFalse(newState2.updated1)

        dispatcher.invoke(MockAction.Update1Action)

        assertTrue(newState1.updated1)
        assertFalse(newState2.updated1)
    }

    @Test fun `GIVEN one subscription subscribed and one subscription unsubscribed WHEN dispatch update action from unsubscribed THEN state updates subscribed subscription`() {
        lateinit var dispatcher: Dispatch<MockAction>
        lateinit var newState1: MockState
        lateinit var newState2: MockState
        store.subscribe { state, _ ->
            newState1 = state
        }
        val unsubscribe = store.subscribe { state, dispatch ->
            newState2 = state
            dispatcher = dispatch
        }
        unsubscribe.invoke()
        assertFalse(newState1.updated1)
        assertFalse(newState2.updated1)

        dispatcher.invoke(MockAction.Update1Action)

        assertTrue(newState1.updated1)
        assertFalse(newState2.updated1)
    }

    @Test fun `GIVEN store with dispatching middleware WHEN dispatch update action THEN middleware dispatches to update state`() {
        lateinit var dispatcher: Dispatch<MockAction>
        lateinit var newState: MockState
        store.subscribe { state, dispatch ->
            newState = state
            dispatcher = dispatch
        }
        assertFalse(newState.updated1)
        assertFalse(newState.middlewareCalled)

        dispatcher.invoke(MockAction.Update1Action)

        assertTrue(newState.updated1)
        assertTrue(newState.middlewareCalled)
    }
}
