package com.vinted.actioncable.client.kotlin

import com.vinted.actioncable.client.kotlin.ConnectionMonitor.Companion.STALE_THRESHOLD
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

class ConnectionMonitorTest {

    private val reconnectionAttemptTimePeriod = STALE_THRESHOLD * 1000L + 4000L
    private val connection = mock<Connection>()

    @Test
    fun connected_noPingForSomeTime_reconnectionAttempt() = runBlocking {
        val connectionMonitor = ConnectionMonitor(connection, Connection.Options())

        connectionMonitor.start()
        delay(reconnectionAttemptTimePeriod)

        verify(connection).reopen()
    }

    @Test
    fun connected_pingAfterSomeTime_noReconnectionAttempt() = runBlocking {
        val connectionMonitor = ConnectionMonitor(connection, Connection.Options())

        connectionMonitor.start()
        delay(reconnectionAttemptTimePeriod / 2)
        connectionMonitor.recordPing()
        delay(reconnectionAttemptTimePeriod / 2)

        verify(connection, never()).reopen()
    }

    @Test
    fun connected_connectionTerminated_noReconnectionAttempt() = runBlocking {
        val connectionMonitor = ConnectionMonitor(connection, Connection.Options())

        connectionMonitor.start()
        connectionMonitor.terminate()
        delay(reconnectionAttemptTimePeriod)

        verify(connection, never()).reopen()
    }

    @Test
    fun connected_connectionLost_retryDisabled_noReconnectionAttempt() = runBlocking {
        val options = Connection.Options().apply { reconnectionMaxAttempts = 0 }
        val connectionMonitor = ConnectionMonitor(connection, options)

        connectionMonitor.start()
        delay(reconnectionAttemptTimePeriod)

        verify(connection, never()).reopen()
    }

    @Test
    fun connected_error_reconnected_reconnectionMaxAttemptsReset() = runBlocking {
        val options = Connection.Options().apply { reconnectionMaxAttempts = 1 }
        val connectionMonitor = ConnectionMonitor(connection, options)

        connectionMonitor.start()
        delay(reconnectionAttemptTimePeriod)
        connectionMonitor.recordConnect()
        delay(reconnectionAttemptTimePeriod)

        verify(connection, times(2)).reopen()
    }
}
