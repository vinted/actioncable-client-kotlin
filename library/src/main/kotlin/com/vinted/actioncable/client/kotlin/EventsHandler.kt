package com.vinted.actioncable.client.kotlin

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class EventsHandler : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    fun handle(operation: suspend () -> Unit) = launch {
        operation.invoke()
    }

    fun handleWithDelay(operation: suspend () -> Unit, duration: Long) = launch {
        delay(duration)
        operation.invoke()
    }

    fun stop() {
        coroutineContext.cancel()
    }
}
