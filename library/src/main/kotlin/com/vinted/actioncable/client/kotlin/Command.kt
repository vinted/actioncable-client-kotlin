package com.vinted.actioncable.client.kotlin

import com.google.gson.GsonBuilder

data class Command(
        private val command: String,
        private val identifier: String,
        private val data: Map<String, Any?> = mapOf()
) {

    fun toJsonString(): String {
        val serializer = GsonBuilder().create()
        val jsonData = mutableMapOf("command" to command, "identifier" to identifier)
        if (!data.isEmpty()) {
            jsonData["data"] = serializer.toJson(data)
        }
        return serializer.toJson(jsonData)
    }

    companion object {
        fun subscribe(identifier: String) = Command("subscribe", identifier)
        fun unsubscribe(identifier: String) = Command("unsubscribe", identifier)
        fun message(identifier: String, data: Map<String, Any?>) =
                Command("message", identifier, data)
    }
}
