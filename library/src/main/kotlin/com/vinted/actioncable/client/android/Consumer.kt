package com.vinted.actioncable.client.android

import com.vinted.actioncable.client.android.Message.Type.*
import java.net.URI

/**
 * The Consumer establishes the connection to a server-side Ruby Connection object.
 * Once established, the ConnectionMonitor will ensure that its properly maintained through heartbeats and checking for stale updates.
 * The Consumer instance is also the gateway to establishing subscriptions to desired channels.
 *
 * @property subscriptions Subscriptions container.
 *
 * ```
 * val consumer = ActionCable.createConsumer(uri, options)
 * val appearanceChannel = Channel("AppearanceChannel")
 * val subscription = consumer.subscriptions.create(appearanceChannel)
 * ```
 */
class Consumer(
        uri: URI,
        options: Options = Options()
) {
    /**
     * Consumer options.
     *
     * @property connection Connection options.
     *
     * ```
     * val options = Consumer.Options()
     * options.connection.reconnection = true
     * options.connection.query = mapOf("user_id" to "1")
     * ```
     */
    data class Options(val connection: Connection.Options = Connection.Options())

    val subscriptions: Subscriptions = Subscriptions(this)

    private val connection: Connection = Connection(uri, options.connection)

    private val connectionMonitor: ConnectionMonitor = ConnectionMonitor(connection, options.connection)

    init {
        connection.onOpen = {
        }

        connection.onMessage = { jsonString ->
            val parsedMessage = Message.createFromJsonString(jsonString)
            when (parsedMessage.messageType) {
                WELCOME -> {
                    connectionMonitor.recordConnect()
                    subscriptions.reload()
                }
                PING -> connectionMonitor.recordPing()
                CONFIRMATION -> subscriptions.notifyConnected(parsedMessage.identifier!!)
                REJECTION -> subscriptions.reject(parsedMessage.identifier!!)
                MESSAGE -> subscriptions.notifyReceived(parsedMessage.identifier!!, parsedMessage.body)
            }
        }

        connection.onClose = {
            subscriptions.notifyDisconnected()
            connectionMonitor.recordDisconnect()
        }

        connection.onFailure = { error ->
            subscriptions.notifyFailed(error)
        }
    }

    /**
     * Establish connection.
     */
    fun connect() {
        connection.open()
        connectionMonitor.start()
    }

    /**
     * Disconnect the underlying connection.
     */
    fun disconnect() {
        connection.terminate()
        connectionMonitor.terminate()
    }

    fun send(command: Command) = connection.send(command)
}
