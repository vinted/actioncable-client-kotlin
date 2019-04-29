package com.vinted.actioncable.client.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChannelTest {

    @Test
    fun createWithIllegalParameterKey() {
        assertThrows<IllegalArgumentException> {
            Channel("AppearanceChannel", mapOf("channel" to 1))
        }
    }

    @Test
    fun identifier() {
        val channel = Channel("AppearanceChannel")
        assertEquals("{\"channel\":\"AppearanceChannel\"}", channel.identifier)
    }

    @Test
    fun identifierWithParams() {
        val channel = Channel("AppearanceChannel", mapOf("a" to 1, "b" to "B", "c" to false, "d" to mapOf("e" to 1)))
        assertEquals("{\"a\":1,\"b\":\"B\",\"c\":false,\"d\":{\"e\":1},\"channel\":\"AppearanceChannel\"}", channel.identifier)
    }
}
