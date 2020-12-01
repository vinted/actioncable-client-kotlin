package com.vinted.actioncable.client.kotlin

import com.vinted.actioncable.client.kotlin.utils.MockWebServerResponseFactory
import com.vinted.actioncable.client.kotlin.utils.TIMEOUT
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI

class SubscriptionsTest {

    private val channel1 = Channel(channel = "foo", params = mapOf())
    private val channel2 = Channel(channel = "bar", params = mapOf())

    @Test
    fun subscriptionCreationAndRemoval() = runBlocking {
        withTimeout(TIMEOUT) {
            val events = Channel<String>()
            val mockWebServer = initializeMockServer(events)
            val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))

            val subscription1 = consumer.subscriptions.create(Channel("CommentsChannel"))
            val subscription2 = consumer.subscriptions.create(Channel("NotificationChannel"))
            subscription1.onConnected = {
                launch {
                    events.send(SUCCESSFUL_CONNECTION_MESSAGE)
                }
            }
            subscription2.onConnected = {
                launch {
                    events.send(SUCCESSFUL_CONNECTION_MESSAGE)
                }
            }
            consumer.connect()

            assertEquals(SUCCESSFUL_CONNECTION_MESSAGE, events.receive())
            assertEquals(SUCCESSFUL_CONNECTION_MESSAGE, events.receive())

            consumer.subscriptions.remove(subscription1)

            assertEquals(false, consumer.subscriptions.contains(subscription1))
            assertEquals(true, consumer.subscriptions.contains(subscription2))
            assertEquals("{\"command\":\"unsubscribe\",\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\"}", events.receive())
            events.close()
            mockWebServer.shutdown()
        }
    }

    @Test
    fun subscriptionCreationAfterConsumerConnection() = runBlocking {
        withTimeout(TIMEOUT) {
            val events = Channel<String>()
            val mockWebServer = initializeMockServer(events)
            val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))

            consumer.connect()
            delay(SERVER_RESPONSE_DELAY)
            val subscription = consumer.subscriptions.create(Channel("CommentsChannel"))
            subscription.onConnected = {
                launch {
                    events.send(SUCCESSFUL_CONNECTION_MESSAGE)
                }
            }

            assertEquals(SUCCESSFUL_CONNECTION_MESSAGE, events.receive())
            events.close()
            mockWebServer.shutdown()
        }
    }

    @Test
    fun removeAllSubscriptions() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val consumer = Consumer(URI("/"))

                val subscription1 = consumer.subscriptions.create(channel1)
                val subscription2 = consumer.subscriptions.create(channel2)
                subscription1.onDisconnected = {
                    launch {
                        events.send(SUCCESSFUL_DISCONNECTION_MESSAGE)
                    }
                }
                subscription2.onDisconnected = {
                    launch {
                        events.send(SUCCESSFUL_DISCONNECTION_MESSAGE)
                    }
                }
                consumer.subscriptions.removeAll()

                assertFalse(consumer.subscriptions.contains(subscription1))
                assertFalse(consumer.subscriptions.contains(subscription2))
                assertEquals(SUCCESSFUL_DISCONNECTION_MESSAGE, events.receive())
                assertEquals(SUCCESSFUL_DISCONNECTION_MESSAGE, events.receive())
                events.close()
            }
        }
    }

    @Test
    fun containsAddedSubscriptions() {
        val consumer = Consumer(URI("/"))

        val subscription1 = consumer.subscriptions.create(channel1)
        val subscription2 = consumer.subscriptions.create(channel2)

        assertTrue(consumer.subscriptions.contains(subscription1))
        assertTrue(consumer.subscriptions.contains(subscription2))
    }

    private fun CoroutineScope.initializeMockServer(events: Channel<String>): MockWebServer {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(MockWebServerResponseFactory.getMockResponse(scope = this, channel = events))
        mockWebServer.start()
        return mockWebServer
    }

    companion object {
        private const val SERVER_RESPONSE_DELAY = 1000L
        private const val SUCCESSFUL_CONNECTION_MESSAGE = "onConnected"
        private const val SUCCESSFUL_DISCONNECTION_MESSAGE = "disconnected"
    }
}
