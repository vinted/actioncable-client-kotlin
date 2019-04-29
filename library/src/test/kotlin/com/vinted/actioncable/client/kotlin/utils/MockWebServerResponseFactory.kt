package com.vinted.actioncable.client.kotlin.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse

object MockWebServerResponseFactory {

    fun getMockResponse(
            scope: CoroutineScope,
            channel: Channel<String>,
            subscriptionJson: String = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"type\":\"confirm_subscription\"}",
            helloJson: String = ""
    ): MockResponse {
        return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            private var currentWebSocket: WebSocket? = null

            override fun onOpen(webSocket: WebSocket, response: Response) {
                currentWebSocket = webSocket
                currentWebSocket?.send("{\"type\":\"welcome\"}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch {
                    when {
                        text.contains("unsubscribe") -> channel.send(text)
                        text.contains("subscribe") -> currentWebSocket?.send(subscriptionJson)
                        text.contains("hello") -> {
                            if (helloJson.isNotEmpty()) {
                                currentWebSocket?.send("{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"message\":{\"foo\":\"bar\"}}")
                            } else {
                                channel.send("onMessage:$text")
                            }
                        }
                    }
                }
            }
        })
    }
}
