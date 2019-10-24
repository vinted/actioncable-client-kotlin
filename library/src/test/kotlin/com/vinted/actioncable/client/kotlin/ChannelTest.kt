package com.vinted.actioncable.client.kotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
        assertEquals("{\"a\":1,\"b\":\"B\",\"c\":false,\"channel\":\"AppearanceChannel\",\"d\":{\"e\":1}}", channel.identifier)
    }

    @Test
    fun twoChannelsWithTheSameParams_identifiersSame() {
        val channel1 = Channel(channel = "fooChannel", params = mapOf("param1" to "true", "param2" to "false"))
        val channel2 = Channel(channel = "fooChannel", params = mapOf("param1" to "true", "param2" to "false"))

        assertEquals(channel1.identifier, channel2.identifier)
    }

    @Test
    fun twoChannelsWithTheSameParams_differentParamsOrder_identifiersSame() {
        val channel1 = Channel(channel = "fooChannel", params = mapOf("param1" to "true", "param2" to "false"))
        val channel2 = Channel(channel = "fooChannel", params = mapOf("param2" to "false", "param1" to "true"))

        assertEquals(channel1.identifier, channel2.identifier)
    }

    @Test
    fun twoChannelsWithTheSameName_differentParams_identifiersNotTheSame() {
        val channel1 = Channel(channel = "fooChannel", params = mapOf("param1" to "true"))
        val channel2 = Channel(channel = "fooChannel", params = mapOf("param1" to "false"))

        assertNotEquals(channel1.identifier, channel2.identifier)
    }
}
