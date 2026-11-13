package com.cuso.tailor.utils

import android.util.Log
import com.cuso.tailor.model.topBarModels.NotificationItemDto
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopbarSocketManager @Inject constructor() {

    private var socket: Socket? = null
    private val gson = Gson()
    private val okHttpClient = OkHttpClient()

    private val _incomingNotifications = MutableSharedFlow<NotificationItemDto>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingNotifications: SharedFlow<NotificationItemDto> = _incomingNotifications.asSharedFlow()

    private val _agendaRefreshSignal = MutableSharedFlow<Unit>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val agendaRefreshSignal: SharedFlow<Unit> = _agendaRefreshSignal.asSharedFlow()

    fun connect(
        rawBaseUrl: String,
        accessToken: String,
        organizationId: String,
        userId: String = ""
    ) {
        if (socket?.connected() == true || accessToken.isBlank()) return

        disconnect()

        try {
            // Strip "/api" or any path so Socket.IO connects to the root server URL
            val parsedUri = URI(rawBaseUrl.trim())
            val socketRootUrl = "${parsedUri.scheme}://${parsedUri.authority}"

            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 2000L
                reconnectionDelayMax = 10000L
                // Allow default transports (polling -> websocket) just like Web
                transports = arrayOf("polling", "websocket")
                // Attach OkHttpClient so Android Studio Network Inspector traces socket.io calls
                callFactory = okHttpClient
                webSocketFactory = okHttpClient
                auth = buildMap {
                    put("token", accessToken)
                    if (organizationId.isNotBlank()) put("organizationId", organizationId)
                    if (userId.isNotBlank()) put("memberId", userId)
                }
                extraHeaders = mapOf(
                    "Authorization" to listOf("Bearer $accessToken")
                )
            }

            Log.d("TopbarSocket", "Connecting to: $socketRootUrl")

            socket = IO.socket(socketRootUrl, options).apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d("TopbarSocket", "Socket CONNECTED! id=${id()}")
                    // Join org room if backend listens for join events
                    if (organizationId.isNotBlank()) {
                        emit("join", organizationId)
                    }
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e("TopbarSocket", "Socket CONNECT_ERROR: ${args.joinToString()}")
                }

                on(Socket.EVENT_DISCONNECT) { args ->
                    Log.w("TopbarSocket", "Socket DISCONNECTED: ${args.joinToString()}")
                }

                val handleNotification: (Array<Any>) -> Unit = { args ->
                    val rawJson = args.firstOrNull()?.toString()
                    Log.d("TopbarSocket", "Notification event received: $rawJson")
                    if (!rawJson.isNullOrBlank()) {
                        try {
                            val dto = gson.fromJson(rawJson, NotificationItemDto::class.java)
                            _incomingNotifications.tryEmit(dto)
                        } catch (e: Exception) {
                            Log.e("TopbarSocket", "Parse error: ${e.message}")
                        }
                    }
                }

                // Listen to common notification event names
                on("notification:new", handleNotification)
                on("new_notification", handleNotification)
                on("notification", handleNotification)

                // Listen to common agenda update event names
                on("agenda:updated") { _agendaRefreshSignal.tryEmit(Unit) }
                on("business-agenda:updated") { _agendaRefreshSignal.tryEmit(Unit) }

                connect()
            }
        } catch (e: Exception) {
            Log.e("TopbarSocket", "Socket init failed: ${e.message}", e)
        }
    }

    fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
    }
}