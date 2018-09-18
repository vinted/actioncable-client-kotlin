package com.vinted.actioncable.client.android

import java.net.URI

object ActionCable {
    /**
     * Create an actioncable consumer.
     */
    fun createConsumer(
            uri: URI,
            options: Consumer.Options = Consumer.Options()
    ) = Consumer(uri, options)
}
