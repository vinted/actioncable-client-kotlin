package com.vinted.actioncable.client.kotlin

import com.google.gson.GsonBuilder
import com.vinted.actioncable.client.kotlin.utils.MockWebServerResponseFactory
import com.vinted.actioncable.client.kotlin.utils.TIMEOUT
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class SubscriptionTest {

    @Test
    fun identifier() {
        val consumer = Consumer(URI("ws://example.com:28080"))
        val channel = Channel("CommentsChannel")
        val subscription = Subscription(consumer, channel)

        assertEquals("{\"channel\":\"CommentsChannel\"}", subscription.identifier)
    }

    @Test
    fun onRejected() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                mockWebServer.enqueue(
                        MockWebServerResponseFactory.getMockResponse(
                                scope = this,
                                subscriptionJson = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"type\":\"reject_subscription\"}",
                                channel = events
                        )
                )
                mockWebServer.start()

                val channel = Channel("CommentsChannel")
                val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))
                val subscription = consumer.subscriptions.create(channel)
                subscription.onRejected = {
                    launch {
                        events.send("onRejected")
                    }
                }
                consumer.connect()

                assertEquals("onRejected", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun onReceived() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                mockWebServer.enqueue(
                        MockWebServerResponseFactory.getMockResponse(
                                scope = this,
                                channel = events,
                                helloJson = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"message\":{\"foo\":\"bar\"}}"
                        )
                )
                mockWebServer.start()

                val channel = Channel("CommentsChannel")
                val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))
                val subscription = consumer.subscriptions.create(channel)
                subscription.onConnected = {
                    subscription.perform("hello")
                }
                subscription.onReceived = { data ->
                    launch {
                        events.send("onReceived:${(GsonBuilder().create().toJson(data))}")
                    }
                }
                consumer.connect()

                assertEquals("onReceived:{\"foo\":\"bar\"}", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun onFailed() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                val mockResponse = MockResponse()
                mockResponse.setResponseCode(500)
                mockResponse.status = "HTTP/1.1 500 Internal Server Error"
                mockWebServer.enqueue(mockResponse)
                mockWebServer.start()

                val channel = Channel("CommentsChannel")
                val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))
                val subscription = consumer.subscriptions.create(channel)
                subscription.onFailed = {
                    launch {
                        events.send("onFailed")
                    }
                }
                consumer.connect()

                assertEquals("onFailed", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun performWithoutParams() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                mockWebServer.enqueue(MockWebServerResponseFactory.getMockResponse(scope = this, channel = events))
                mockWebServer.start()

                val channel = Channel("CommentsChannel")
                val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))
                val subscription = consumer.subscriptions.create(channel)
                subscription.onConnected = {
                    subscription.perform("hello")
                }
                consumer.connect()

                assertEquals("onMessage:{\"command\":\"message\",\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"data\":\"{\\\"action\\\":\\\"hello\\\"}\"}", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun performWithParams() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                mockWebServer.enqueue(MockWebServerResponseFactory.getMockResponse(scope = this, channel = events))
                mockWebServer.start()

                val channel = Channel("CommentsChannel")
                val consumer = Consumer(URI(mockWebServer.url("/").toUri().toString()))
                val subscription = consumer.subscriptions.create(channel)
                subscription.onConnected = {
                    subscription.perform("hello", mapOf("foo" to "bar"))
                }
                consumer.connect()

                assertEquals("onMessage:{\"command\":\"message\",\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"data\":\"{\\\"foo\\\":\\\"bar\\\",\\\"action\\\":\\\"hello\\\"}\"}", events.receive())

                mockWebServer.shutdown()
            }
        }
    }
}
