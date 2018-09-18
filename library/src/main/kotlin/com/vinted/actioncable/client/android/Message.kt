package com.vinted.actioncable.client.android

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

data class Message(
        private val type: String?,
        val identifier: String?,
        @SerializedName("message") val body: Any?
) {

    val messageType: Type
        get() = type?.let { type -> Type.values().first { it.type == type } } ?: Type.MESSAGE

    enum class Type(val type: String?) {
        WELCOME("welcome"),
        PING("ping"),
        CONFIRMATION("confirm_subscription"),
        REJECTION("reject_subscription"),
        MESSAGE(null)
    }

    companion object {
        fun createFromJsonString(jsonString: String): Message =
                GsonBuilder().create().fromJson<Message>(jsonString, Message::class.java)
    }
}
