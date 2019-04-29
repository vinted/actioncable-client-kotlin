package com.vinted.actioncable.client.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandTest {

    @Test
    fun subscribe() {
        val command = Command.subscribe("myidentifier")
        assertEquals("{\"command\":\"subscribe\",\"identifier\":\"myidentifier\"}", command.toJsonString())
    }

    @Test
    fun unsubscribe() {
        val command = Command.unsubscribe("myidentifier")
        assertEquals("{\"command\":\"unsubscribe\",\"identifier\":\"myidentifier\"}", command.toJsonString())
    }

    @Test
    fun message() {
        val command = Command.message("myidentifier", mapOf("foo" to "bar"))
        assertEquals("{\"command\":\"message\",\"identifier\":\"myidentifier\",\"data\":\"{\\\"foo\\\":\\\"bar\\\"}\"}", command.toJsonString())
    }
}
