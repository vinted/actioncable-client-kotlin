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

class ConnectionTest {

    @Test
    fun createUriAndOptions() {
        val connection = Connection(URI("ws://example.com:28080"), Connection.Options())
        assertNotNull(connection)
    }

    @Test
    fun onOpen() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()
                val mockWebServer = MockWebServer()
                val mockResponse = MockResponse().withWebSocketUpgrade(object : WebSocketListener() {})
                mockWebServer.enqueue(mockResponse)
                mockWebServer.start()

                val connection = Connection(URI(mockWebServer.url("/").toUri().toString()), Connection.Options())
                connection.onOpen = {
                    launch {
                        events.send("onOpen")
                    }
                }
                connection.open()

                assertEquals("onOpen", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun onMessage() {
        runBlocking {
            withTimeout(TIMEOUT) {
                val events = Channel<String>()

                val mockWebServer = MockWebServer()
                val mockResponse = MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send("{}")
                    }
                })
                mockWebServer.enqueue(mockResponse)
                mockWebServer.start()

                val connection = Connection(URI(mockWebServer.url("/").toUri().toString()), Connection.Options())
                connection.onMessage = { text ->
                    launch {
                        events.send("onMessage:$text")
                    }

                }
                connection.open()

                assertEquals("onMessage:{}", events.receive())

                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun onFailureWhenInternalServerErrorReceived() = runBlocking {
        val events = Channel<String>()
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(500)
        mockResponse.status = "HTTP/1.1 500 Internal Server Error"
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()

        val connection = Connection(URI(mockWebServer.url("/").toUri().toString()), Connection.Options())
        connection.onFailure = {
            launch {
                events.send("onFailure")
            }
        }
        connection.open()

        assertEquals("onFailure", events.receive())

        mockWebServer.shutdown()
    }
}
