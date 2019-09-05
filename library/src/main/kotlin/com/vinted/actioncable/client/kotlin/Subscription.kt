package com.vinted.actioncable.client.kotlin

typealias ConnectedHandler = () -> Unit
typealias RejectedHandler = () -> Unit
typealias ReceivedHandler = (data: Any?) -> Unit
typealias DisconnectedHandler = () -> Unit
typealias FailedHandler = (e: Throwable) -> Unit

/**
 * Subscription provides a number of callbacks and a method for calling remote procedure calls
 * on the corresponding Channel instance on the server side.
 */
class Subscription constructor(private val consumer: Consumer, channel: Channel) {
    /**
     * Callback called when the subscription has been successfully completed.
     */
    var onConnected: ConnectedHandler? = null

    /**
     * Callback called when the subscription is rejected by the server.
     */
    var onRejected: RejectedHandler? = null

    /**
     * Callback called when the subscription receives data from the server.
     */
    var onReceived: ReceivedHandler? = null

    /**
     * Callback called when the subscription has been closed.
     */
    var onDisconnected: DisconnectedHandler? = null

    /**
     * Callback called when the subscription encounters any error.
     */
    var onFailed: FailedHandler? = null

    val identifier = channel.identifier

    /**
     * Call remote procedure calls on the corresponding Channel instance on the server.
     *
     * @param action Procedure name to perform
     * @param params Parameters passed to procedure
     */
    fun perform(action: String, params: Map<String, Any?> = mapOf()) {
        require(!params.containsKey("action")) { "action is reserved key" }
        val data = params.toMutableMap()
        data["action"] = action
        consumer.send(Command.message(identifier, data))
    }

    fun notifyConnected() {
        onConnected?.invoke()
    }

    fun notifyRejected() {
        onRejected?.invoke()
    }

    fun notifyReceived(data: Any?) {
        onReceived?.invoke(data)
    }

    fun notifyDisconnected() {
        onDisconnected?.invoke()
    }

    fun notifyFailed(error: Throwable) {
        onFailed?.invoke(error)
    }
}
