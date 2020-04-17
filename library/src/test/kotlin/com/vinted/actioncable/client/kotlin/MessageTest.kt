package com.vinted.actioncable.client.kotlin

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MessageTest {

    @Test
    fun createFromJsonStringForWelcome() {
        val jsonString = "{\"type\":\"welcome\"}"
        val message = Message.createFromJsonString(jsonString)

        assertEquals(Message.Type.WELCOME, message.messageType)
        assertEquals(null, message.identifier)
        assertEquals(null, message.body)
    }

    @Test
    fun createFromJsonStringForPing() {
        val jsonString = "{\"type\":\"ping\",\"message\":1505265037}"
        val message = Message.createFromJsonString(jsonString)

        assertEquals(Message.Type.PING, message.messageType)
        assertEquals(null, message.identifier)
        assertEquals(1505265037, Math.round(message.body as Double))
    }

    @Test
    fun createFromJsonStringForConfirmation() {
        val jsonString = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"type\":\"confirm_subscription\"}"
        val message = Message.createFromJsonString(jsonString)

        assertEquals(Message.Type.CONFIRMATION, message.messageType)
        assertEquals("{\"channel\":\"CommentsChannel\"}", message.identifier)
        assertEquals(null, message.body)
    }

    @Test
    fun createFromJsonStringForRejection() {
        val jsonString = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"type\":\"reject_subscription\"}"
        val message = Message.createFromJsonString(jsonString)

        assertEquals(Message.Type.REJECTION, message.messageType)
        assertEquals("{\"channel\":\"CommentsChannel\"}", message.identifier)
        assertEquals(null, message.body)
    }

    @Test
    fun createFromJsonStringForDisconnect() {
        val jsonString = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"type\":\"disconnect\"}"
        val message = Message.createFromJsonString(jsonString)

        assertEquals(Message.Type.DISCONNECT, message.messageType)
        assertEquals("{\"channel\":\"CommentsChannel\"}", message.identifier)
        assertEquals(null, message.body)
    }

    @Test
    fun createFromJsonStringForMessage() {
        val jsonString = "{\"identifier\":\"{\\\"channel\\\":\\\"CommentsChannel\\\"}\",\"message\":{\"foo\":\"bar\"}}"
        val message = Message.createFromJsonString(jsonString)

        assertEquals(Message.Type.MESSAGE, message.messageType)
        assertEquals("{\"channel\":\"CommentsChannel\"}", message.identifier)
        assertEquals("{\"foo\":\"bar\"}", GsonBuilder().create().toJson(message.body))
    }
}
