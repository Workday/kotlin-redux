# kotlin-redux

[![Build Status](https://travis-ci.com/Workday/kotlin-redux.svg?token=QBXyzM3XGY5u6T692bcA&branch=main)](https://travis-ci.com/github/Workday/kotlin-redux)

This library is a simplified implementation of Redux written in Kotlin for Android. 

Redux is an architecture that was originally created in JavaScript: https://redux.js.org/. It follows a strict, unidirectional flow of data, and makes state predictable. It achieves this by ensuring the state is the single source of truth and immutable, and that changes to the state are done through pure functions.

![Diagram of Redux](images/redux.png)

## Installation

Add the following lines to your build.gradle file, replacing $kotlin_redux_version with latest version from Maven Central.

```
repositories {
    mavenCentral()
}

dependencies {
    compile "com.workday:kotlin-redux:$kotlin_redux_version"
    compile "com.workday:kotlin-redux:$kotlin_redux_version"
}
```

## How To

To create a store:

```
val store = SingleThreadedStore<MainState, CounterAction>(
  state = MainState(...),
  reducer = MainReducer(),
  middleware = listOf(ExampleMiddleware(), AnotherMiddleware())
)
```

To subscribe to the store:

```
val storeUnsubscriber = store.subscribe { currentState, dispatch ->
  ...
}
```

Ensure that you also unsubscribe from the store by calling:

```
storeUnsubscriber.invoke()
```

Avoid dispatching multiple times in a row as per Redux best practices. To dispatch actions:

```
dispatch(ExampleAction)
```

## Sample

See the sample app in the [sample](/sample) module for additional reference. The sample app is a simple counter, which shows a count and allows the user to increment and decrement the count.
