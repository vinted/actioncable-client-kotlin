package com.vinted.actioncable.client.kotlin

import com.google.gson.GsonBuilder

/**
 * Channel is a descriptor of a channel.
 *
 * @author hosopy <https://github.com/hosopy>
 */
data class Channel(
        val channel: String,
        private val params: Map<String, Any?> = mapOf()
) {

    val identifier: String

    init {
        require(!params.containsKey("channel")) { "channel is a reserved key" }

        identifier = GsonBuilder().create()
                .toJson(params.toMutableMap().apply { put("channel", channel) }.toSortedMap())
    }
}
