package com.example.zaradashboardapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttManager(
    private val context: Context,
    private val onStatusChanged: (ConnectionStatus) -> Unit,
    private val onMessageReceived: (String, String) -> Unit
) {
    private var mqttClient: MqttClient? = null
    private val persistence = MemoryPersistence()
    private val TAG = "MqttManager"

    fun connect(settings: SettingsManager.MqttSettings) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        
        val serverUris = mutableListOf<String>()
        
        if (isWifi) {
            serverUris.add("tcp://${settings.localIp}:${settings.port}")
            if (settings.remoteIp.isNotEmpty()) {
                serverUris.add("tcp://${settings.remoteIp}:${settings.port}")
            }
        } else if (settings.remoteIp.isNotEmpty()) {
            serverUris.add("tcp://${settings.remoteIp}:${settings.port}")
        } else {
            serverUris.add("tcp://${settings.localIp}:${settings.port}")
        }

        attemptConnection(serverUris, settings, 0)
    }

    private fun attemptConnection(uris: List<String>, settings: SettingsManager.MqttSettings, index: Int) {
        if (index >= uris.size) {
            onStatusChanged(ConnectionStatus.DISCONNECTED)
            return
        }

        val uri = uris[index]
        val isLocal = uri.contains(settings.localIp)
        
        onStatusChanged(ConnectionStatus.CONNECTING)
        
        try {
            mqttClient?.let {
                if (it.isConnected) it.disconnect()
                it.close()
            }
            mqttClient = MqttClient(uri, MqttClient.generateClientId(), persistence)
            
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 30
                isAutomaticReconnect = true
                if (settings.username.isNotEmpty()) {
                    userName = settings.username
                    password = settings.password.toCharArray()
                }
            }

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d(TAG, "Connection established to $serverURI (reconnect: $reconnect)")
                    onStatusChanged(if (isLocal) ConnectionStatus.CONNECTED_LOCAL else ConnectionStatus.CONNECTED_REMOTE)
                    mqttClient?.subscribe("${settings.baseTopic}/#")
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "Connection lost: ${cause?.message}", cause)
                    onStatusChanged(ConnectionStatus.DISCONNECTED)
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    onMessageReceived(topic ?: "", message?.toString() ?: "")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            mqttClient?.connect(options)
            
        } catch (e: Exception) {
            val errorMsg = if (e is MqttException) {
                "Errore MQTT (${e.reasonCode}): ${e.message}"
            } else {
                "Errore: ${e.message}"
            }
            Log.e(TAG, "Failed to connect to $uri: $errorMsg")
            attemptConnection(uris, settings, index + 1)
        }
    }

    fun publish(topic: String, message: String, retained: Boolean = false) {
        try {
            if (mqttClient?.isConnected == true) {
                val mqttMessage = MqttMessage(message.toByteArray()).apply {
                    isRetained = retained
                }
                mqttClient?.publish(topic, mqttMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Publish failed: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            mqttClient?.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect failed: ${e.message}")
        }
    }
}
