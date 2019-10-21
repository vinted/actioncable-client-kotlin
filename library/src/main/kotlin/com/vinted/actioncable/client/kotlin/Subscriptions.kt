package com.vinted.actioncable.client.kotlin

/**
 * Collection class for creating (and internally managing) channel subscriptions.
 *
 * ```
 * // Create a subscription instance
 * val subscription = consumer.subscriptions.create(appearanceChannel)
 *
 * // Remove a subscription instance
 * consumer.subscriptions.remove(subscription)
 * ```
 */
class Subscriptions constructor(private val consumer: Consumer) {

    private val subscriptions = mutableListOf<Subscription>()

    /**
     * Create [Subscription] instance.
     *
     * @param channel Channel to connect
     * @return Subscription instance
     */
    fun create(channel: Channel): Subscription {
        require(channel.identifier !in subscriptions.map { it.identifier }) { "Such subscription already exists" }

        val subscription = Subscription(consumer, channel)

        subscriptions.add(subscription)
        sendSubscribeCommand(subscription)

        return subscription
    }

    /**
     * Remove subscription from collection.
     *
     * @param subscription instance to remove
     */
    fun remove(subscription: Subscription) {
        if (subscriptions.remove(subscription)) {
            consumer.send(Command.unsubscribe(subscription.identifier))
        }
    }

    fun contains(subscription: Subscription) = subscriptions.contains(subscription)

    fun reload() {
        subscriptions.forEach { sendSubscribeCommand(it) }
    }

    fun notifyConnected(identifier: String) {
        subscriptions.filter { it.identifier == identifier }.forEach { it.notifyConnected() }
    }

    fun notifyDisconnected() {
        subscriptions.forEach { it.notifyDisconnected() }
    }

    fun notifyReceived(identifier: String, data: Any?) {
        subscriptions.filter { it.identifier == identifier }.forEach { it.notifyReceived(data) }
    }

    fun notifyFailed(error: Throwable) {
        subscriptions.forEach { it.notifyFailed(error) }
    }

    fun reject(identifier: String) {
        val removal = subscriptions.filter { it.identifier == identifier }
        subscriptions.removeAll(removal)
        removal.forEach { it.notifyRejected() }
    }

    private fun sendSubscribeCommand(subscription: Subscription) {
        consumer.send(Command.subscribe(subscription.identifier))
    }
}
