package com.vinted.actioncable.client.kotlin

import com.vinted.actioncable.client.kotlin.utils.TIMEOUT
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.URI

class ConsumerTest {

    @Test
    fun createWithValidUri() {
        val consumer = Consumer(URI("ws://example.com:28080"))
        assertNotNull(consumer)
    }

    @Test
    fun createWithValidUriAndOptions() {
        val consumer = Consumer(URI("ws://example.com:28080"), Consumer.Options())
        assertNotNull(consumer)
    }

    @Test
    fun subscriptions() {
        val consumer = Consumer(URI("ws://example.com:28080"))
        assertNotNull(consumer.subscriptions)
    }

    @Test
    fun connect() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                val mockResponse = MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        launch {
                            events.send("onOpen")
                        }
                    }
                })
                mockWebServer.enqueue(mockResponse)
                mockWebServer.start()

                val consumer = ActionCable.createConsumer(URI(mockWebServer.url("/").toUri().toString()))
                consumer.connect()

                assertEquals("onOpen", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun send() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()

                val mockWebServer = MockWebServer()
                val mockResponse = MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        launch {
                            events.send("onOpen")
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        launch {
                            events.send("onMessage:$text")
                        }
                    }
                })
                mockWebServer.enqueue(mockResponse)
                mockWebServer.start()

                val consumer = ActionCable.createConsumer(URI(mockWebServer.url("/").toUri().toString()))
                consumer.connect()

                assertEquals("onOpen", events.receive())

                consumer.send(Command.subscribe("identifier"))

                assertEquals("onMessage:{\"command\":\"subscribe\",\"identifier\":\"identifier\"}", events.receive())

                mockWebServer.shutdown()
            }
        }
    }
}
