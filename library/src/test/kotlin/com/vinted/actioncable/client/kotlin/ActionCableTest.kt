package com.vinted.actioncable.client.kotlin

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.net.URI

class ActionCableTest {

    @Test
    fun createWithUri() {
        val consumer = ActionCable.createConsumer(URI("ws://example.com:2808"))
        assertNotNull(consumer)
    }

    @Test
    fun createWithUriAndOptions() {
        val consumer = ActionCable.createConsumer(URI("ws://example.com:2808"), Consumer.Options())
        assertNotNull(consumer)
    }
}
