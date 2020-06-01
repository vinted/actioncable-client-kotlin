package com.vinted.actioncable.client.kotlin

import com.vinted.actioncable.client.kotlin.Message.Type.*
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
        private val options: Options = Options()
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
                CONFIRMATION -> if (parsedMessage.identifier != null)  subscriptions.notifyConnected(parsedMessage.identifier)
                REJECTION -> if (parsedMessage.identifier != null)  subscriptions.reject(parsedMessage.identifier)
                MESSAGE -> if (parsedMessage.identifier != null) subscriptions.notifyReceived(parsedMessage.identifier, parsedMessage.body)
                DISCONNECT -> subscriptions.notifyDisconnected()
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
        if (options.connection.reconnection) {
            connectionMonitor.start()
        }
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
