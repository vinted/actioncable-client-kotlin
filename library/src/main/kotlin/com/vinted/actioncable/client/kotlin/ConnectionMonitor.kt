package com.vinted.actioncable.client.kotlin

import android.os.Handler
import android.os.HandlerThread
import android.os.Message

class ConnectionMonitor(
        private val connection: Connection,
        private val options: Connection.Options
) {

    private val pollHandlerThread by lazy {
        HandlerThread(ID_POLL_HANDLER_THREAD)
    }
    private val pollHandler by lazy {
        object : Handler(pollHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                if (msg.what == ACTION_POLL) {
                    if (stoppedAt == 0L) {
                        reconnectIfStale()
                    }
                    poll()
                }
            }
        }
    }

    init {
        pollHandlerThread.start()
    }

    private var pingedAt = 0L

    private var disconnectedAt = 0L

    private var startedAt = 0L

    private var stoppedAt = 0L

    private var reconnectAttempts = 0

    private val interval: Long
        get() {
            return Math.max(
                    options.reconnectionDelay, Math.min(
                    options.reconnectionDelayMax,
                    (5.0 * Math.log((reconnectAttempts + 1).toDouble())).toInt()
            )
            ) * 1000L
        }

    private val connectionIsStale: Boolean
        get() = secondsSince(if (pingedAt > 0) pingedAt else startedAt) > STALE_THRESHOLD

    private val disconnectedRecently: Boolean
        get() = disconnectedAt != 0L && secondsSince(disconnectedAt) < STALE_THRESHOLD

    fun recordConnect() {
        reset()
        pingedAt = now()
        disconnectedAt = 0L
    }

    fun recordDisconnect() {
        disconnectedAt = now()
    }

    fun recordPing() {
        pingedAt = now()
    }

    fun start() {
        reset()
        stoppedAt = 0L
        startedAt = now()
        poll()
    }

    fun terminate() {
        stoppedAt = now()
        pollHandler.removeCallbacksAndMessages(null)
        pollHandlerThread.quit()
    }

    private fun poll() {
        pollHandler.sendEmptyMessageDelayed(ACTION_POLL, interval)
    }

    private fun reset() {
        reconnectAttempts = 0
    }

    private fun now(): Long = System.currentTimeMillis()

    private fun reconnectIfStale() {
        if (options.reconnection && connectionIsStale && reconnectAttempts < options.reconnectionMaxAttempts) {
            reconnectAttempts++
            if (!disconnectedRecently) {
                connection.reopen()
            }
        }
    }

    private fun secondsSince(time: Long): Long = (now() - time) / 1000

    companion object {
        private const val ID_POLL_HANDLER_THREAD = "poll_handler_thread"

        private const val ACTION_POLL = 1

        private const val STALE_THRESHOLD = 6
    }
}
