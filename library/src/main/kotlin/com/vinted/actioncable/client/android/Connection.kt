package com.vinted.actioncable.client.android

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import okhttp3.*
import java.io.IOException
import java.net.CookieHandler
import java.net.URI
import java.net.URLEncoder
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext

typealias OkHttpClientFactory = () -> OkHttpClient

class Connection constructor(
        private val uri: URI,
        private val options: Options
) {
    /**
     * Options for connection.
     *
     * @property sslContext SSLContext
     * @property hostnameVerifier HostnameVerifier
     * @property cookieHandler CookieHandler
     * @property query Query parameters to send on handshake.
     * @property headers HTTP Headers to send on handshake.
     * @property reconnection Whether to reconnect automatically. If reconnection is true, the client attempts to reconnect to the server when underlying connection is stale.
     * @property reconnectionMaxAttempts The maximum number of attempts to reconnect.
     * @property reconnectionDelay First delay seconds of reconnection.
     * @property reconnectionDelayMax Max delay seconds of reconnection.
     * @property okHttpClientFactory To use your own OkHttpClient, set this option.
     */
    data class Options(
            var sslContext: SSLContext? = null,
            var hostnameVerifier: HostnameVerifier? = null,
            var cookieHandler: CookieHandler? = null,
            var query: Map<String, String>? = null,
            var headers: Map<String, String>? = null,
            var reconnection: Boolean = false,
            var reconnectionMaxAttempts: Int = 30,
            var reconnectionDelay: Int = 3,
            var reconnectionDelayMax: Int = 30,
            var okHttpClientFactory: OkHttpClientFactory? = null
    )

    private enum class State {
        CONNECTING,
        OPEN,
        CLOSING,
        CLOSED,
        TERMINATING
    }

    var onOpen: () -> Unit = {}
    var onMessage: (jsonString: String) -> Unit = {}
    var onClose: () -> Unit = {}
    var onFailure: (e: Exception) -> Unit = {}

    private var state = State.CONNECTING

    private var webSocket: WebSocket? = null

    private var isReopening = false

    private val actionsHandlerThread by lazy {
        HandlerThread(ID_ACTIONS_HANDLER_THREAD)
    }
    private val actionsHandler by lazy {
        object : Handler(actionsHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    ACTION_OPEN -> performOpen()
                    ACTION_SEND -> doSend(msg.obj)
                    ACTION_INVOKE_ON_OPEN_CALLBACK -> onOpen.invoke()
                    ACTION_INVOKE_ON_MESSAGE_CALLBACK -> onMessage.invoke(msg.obj as String)
                    ACTION_HANDLE_FAILURE -> handleFailure(msg.obj as Throwable?)
                    ACTION_CLOSE -> performClose()
                    ACTION_HANDLE_CLOSURE -> handleClosure()
                }
            }
        }
    }

    init {
        actionsHandlerThread.start()
    }

    fun open() {
        actionsHandler.sendEmptyMessage(ACTION_OPEN)
    }

    private fun performOpen() {
        if (isOpen()) {
            fireOnFailure(IllegalStateException("Must close existing connection before opening"))
        } else {
            doOpen()
        }
    }

    private fun close() {
        actionsHandler.sendEmptyMessage(ACTION_CLOSE)
    }

    fun terminate() {
        state = State.TERMINATING
        close()
    }

    private fun performClose() {
        webSocket?.let { webSocket ->
            if (!isState(State.CLOSING, State.CLOSED)) {
                try {
                    webSocket.close(1000, "connection closed manually")
                    if (state != State.TERMINATING) state = State.CLOSING
                } catch (e: IOException) {
                    fireOnFailure(e)
                } catch (e: IllegalStateException) {
                    fireOnFailure(e)
                }
            }
        }
    }

    fun reopen() {
        if (isState(State.TERMINATING)) return
        if (isState(State.CLOSED)) {
            open()
        } else {
            isReopening = true
            close()
        }
    }

    fun send(data: Any): Boolean {
        if (!isOpen()) return false

        actionsHandler.sendMessage(Message.obtain().apply {
            what = ACTION_SEND
            obj = data
        })

        return true
    }

    private fun isState(vararg states: State) = states.contains(state)

    private fun isOpen() = webSocket?.let { isState(State.OPEN) } ?: false

    private fun doOpen() {
        state = State.CONNECTING

        val httpClientBuilder = (options.okHttpClientFactory?.invoke()
                ?: OkHttpClient()).newBuilder()

        options.sslContext?.let { httpClientBuilder.sslSocketFactory(it.socketFactory) }
        options.hostnameVerifier?.let { httpClientBuilder.hostnameVerifier(it) }

        val urlBuilder = StringBuilder(uri.toString())

        options.query?.let { urlBuilder.append("?${it.toQueryString()}") }

        val requestBuilder = Request.Builder().url(urlBuilder.toString())

        options.headers?.forEach { key, value -> requestBuilder.addHeader(key, value) }

        val request = requestBuilder.build()

        val httpClient = httpClientBuilder.build()

        httpClient.newWebSocket(request, webSocketListener)

        httpClient.dispatcher().executorService().shutdown()
    }

    private fun doSend(data: Any) {
        webSocket?.let { webSocket ->
            try {
                webSocket.send((data as Command).toJsonString())
            } catch (e: IOException) {
                fireOnFailure(e)
            }
        }
    }

    private fun fireOnFailure(error: Exception) {
        onFailure.invoke(error)
        if (isState(State.TERMINATING)) stopHandler()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(openedWebSocket: WebSocket?, response: Response?) {
            state = State.OPEN
            webSocket = openedWebSocket
            actionsHandler.sendEmptyMessage(ACTION_INVOKE_ON_OPEN_CALLBACK)
        }

        override fun onFailure(webSocket: WebSocket?, throwable: Throwable?, response: Response?) {
            actionsHandler.sendMessage(Message.obtain().apply {
                what = ACTION_HANDLE_FAILURE
                obj = throwable
            })
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            text?.also {
                actionsHandler.sendMessage(Message.obtain().apply {
                    what = ACTION_INVOKE_ON_MESSAGE_CALLBACK
                    obj = it
                })
            }
        }

        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
            println("WebSocket#onClose")
            state = State.CLOSED
            actionsHandler.sendEmptyMessage(ACTION_HANDLE_CLOSURE)
        }
    }

    private fun handleFailure(throwable: Throwable?) {
        state = State.CLOSED
        throwable ?: RuntimeException("Unexpected error")
        fireOnFailure(Exception(throwable))
    }

    private fun handleClosure() {
        if (isState(State.TERMINATING)) {
            stopHandler()
            isReopening = false
        }
        state = State.CLOSED

        onClose.invoke()

        if (isReopening) {
            isReopening = false
            open()
        }
    }

    private fun stopHandler() {
        actionsHandler.removeCallbacksAndMessages(null)
        actionsHandlerThread.quit()
    }

    companion object {
        private const val ID_ACTIONS_HANDLER_THREAD = "actions_handler_thread"

        private const val ACTION_OPEN = 1
        private const val ACTION_SEND = 2
        private const val ACTION_INVOKE_ON_OPEN_CALLBACK = 3
        private const val ACTION_INVOKE_ON_MESSAGE_CALLBACK = 4
        private const val ACTION_HANDLE_FAILURE = 5
        private const val ACTION_CLOSE = 6
        private const val ACTION_HANDLE_CLOSURE = 7
    }
}

private fun Map<String, String>.toQueryString(): String {
    return this.keys.asSequence().mapNotNull { key ->
        this[key]?.let {
            "$key=${URLEncoder.encode(this[key], Charsets.UTF_8.toString())}"
        }
    }.joinToString("&")
}
