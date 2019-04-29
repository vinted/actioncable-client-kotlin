package com.vinted.actioncable.client.kotlin

import com.vinted.actioncable.client.kotlin.utils.MockWebServerResponseFactory
import com.vinted.actioncable.client.kotlin.utils.TIMEOUT
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class SubscriptionsTest {

    @Test
    fun subscriptionCreationAndRemoval() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                mockWebServer.enqueue(MockWebServerResponseFactory.getMockResponse(scope = this, channel = events))
                mockWebServer.start()

                val consumer = Consumer(URI(mockWebServer.url("/").uri().toString()))
                val subscription1 = consumer.subscriptions.create(Channel("CommentsChannel"))
                val subscription2 = consumer.subscriptions.create(Channel("NotificationChannel"))
                subscription1.onConnected = {
                    launch {
                        events.send("onConnected")
                    }
                }
                subscription2.onConnected = {
                    launch {
                        events.send("onConnected")
                    }
                }
                consumer.connect()

                assertEquals("onConnected", events.receive())
                assertEquals("onConnected", events.receive())

                consumer.subscriptions.remove(subscription1)

                assertEquals(false, consumer.subscriptions.contains(subscription1))
                assertEquals(true, consumer.subscriptions.contains(subscription2))
                assertEquals("{\"command\":\"unsubscribe\",\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\"}", events.receive())

                mockWebServer.shutdown()
            }
        }
    }
}
