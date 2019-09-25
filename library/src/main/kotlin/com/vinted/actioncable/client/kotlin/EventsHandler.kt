package com.vinted.actioncable.client.kotlin

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext

class EventsHandler : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private val actor = actor<suspend () -> Unit> {
        for (msg in channel) {
            msg.invoke()
        }
    }

    fun handle(operation: suspend () -> Unit) = launch {
        send(operation)
    }

    fun handleWithDelay(operation: suspend () -> Unit, duration: Long) = launch {
        delay(duration)
        send(operation)
    }

    private suspend fun send(operation: suspend () -> Unit) {
        if (!actor.isClosedForSend) {
            actor.send(operation)
        }
    }

    fun stop() {
        actor.close()
        coroutineContext.cancel()
    }
}
