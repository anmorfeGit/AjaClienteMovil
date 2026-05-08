package com.example.ajaclientemovil.network

import android.content.Context
import android.util.Log
import com.example.ajaclientemovil.data.NotifyStatusDTO
import com.example.ajaclientemovil.network.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader



class WebSocketManager(private val context: Context) {
    private val url = "wss://ajaserver.mel0n.dev/api/ws-connection"
    private val gson = Gson()
    private var mStompClient: StompClient? = null

    // Guardamos la suscripción para poder liberarla si fuera necesario
    private var statusSubscription: io.reactivex.disposables.Disposable? = null

    /**
     * Patrón Singleton para mantener una única instancia de WebSocketManager.
     * @param context Contexto de la aplicación.
     * @return Instancia de WebSocketManager.
     */
    companion object {
        @Volatile
        private var INSTANCE: WebSocketManager? = null

        fun getInstance(context: Context): WebSocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Conecta al servidor WebSocket y se suscribe al tópico "/status".
     * @param onMessage Callback que se llama cuando se recibe un mensaje.
     * El parámetro es una lista de objetos NotifyStatusDTO.
     */
    fun connectAndSubscribe(onMessage: (List<NotifyStatusDTO>) -> Unit) {
        if (mStompClient != null && mStompClient!!.isConnected) {
            Log.d("STOMP", " Ya existe una conexión activa. Omitiendo duplicado.")
            return
        }

        val token = SessionManager.getToken(context)
        if (token == null) {
            Log.e("STOMP", " No hay token, no se puede conectar")
            return
        }

        mStompClient?.disconnect()

        mStompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            url,
            mapOf("Cookie" to "JWT_TOKEN=$token")
        )
        //mStompClient?.withServerHeartbeat(20000)?.withClientHeartbeat(20000)

        mStompClient!!.lifecycle().subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> Log.d("STOMP", "CONECTADO")
                LifecycleEvent.Type.ERROR -> Log.e("STOMP", "ERROR: ${lifecycleEvent.exception?.message}")
                LifecycleEvent.Type.CLOSED -> Log.d("STOMP", "CERRADO")
                else -> {}
            }
        }
        statusSubscription?.dispose()

        statusSubscription = mStompClient!!.topic("/status").subscribe({ topicMessage ->
            try {
                val listType = object : TypeToken<List<NotifyStatusDTO>>() {}.type
                val statusList: List<NotifyStatusDTO> = gson.fromJson(topicMessage.payload, listType)
                onMessage(statusList)
            } catch (e: Exception) {
                Log.e("STOMP", "Error al procesar JSON: ${e.message}")
            }
        }, { error ->
            Log.e("STOMP", "Error en la suscripción: ${error.message}")
        })

        mStompClient!!.connect()
    }

    /**
     * Envia un mensaje a través del WebSocket.
     * @param dto Objeto NotifyStatusDTO a enviar.
     * @param isStarting Indica si es un mensaje de inicio o finalización.
     */
    fun sendStatus(dto: NotifyStatusDTO, isStarting: Boolean) {
        if (mStompClient == null || !mStompClient!!.isConnected) {
            Log.e("STOMP", "Intento de enviar mensaje sin conexión activa")
            return
        }

        val destination = if (isStarting) "/api/notify" else "/api/finish"
        val jsonPayload = gson.toJson(dto)

        mStompClient?.send(destination, jsonPayload)?.subscribe({
            Log.d("STOMP", "ENVÍO OK a $destination")
        }, { error ->
            Log.e("STOMP", "FALLO ENVÍO: ${error.message}")
        })
    }

    /**
     * Desconecta del servidor WebSocket.
     * Si no hay conexión activa, no hace nada.
     */
    fun disconnect() {
        statusSubscription?.dispose()
        mStompClient?.disconnect()
        mStompClient = null
    }
}