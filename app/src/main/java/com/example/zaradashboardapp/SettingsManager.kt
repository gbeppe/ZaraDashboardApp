package com.example.zaradashboardapp

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("zara_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LOCAL_IP = "local_ip"
        const val KEY_REMOTE_IP = "remote_ip"
        const val KEY_PORT = "port"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_BASE_TOPIC = "base_topic"

        const val DEFAULT_LOCAL_IP = "192.168.1.20"
        const val DEFAULT_PORT = "1883"
        const val DEFAULT_BASE_TOPIC = "zara/android/domotica"
    }

    data class MqttSettings(
        val localIp: String,
        val remoteIp: String,
        val port: String,
        val username: String,
        val password: String,
        val baseTopic: String
    )

    fun getSettings(): MqttSettings {
        return MqttSettings(
            localIp = prefs.getString(KEY_LOCAL_IP, DEFAULT_LOCAL_IP) ?: DEFAULT_LOCAL_IP,
            remoteIp = prefs.getString(KEY_REMOTE_IP, "") ?: "",
            port = prefs.getString(KEY_PORT, DEFAULT_PORT) ?: DEFAULT_PORT,
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            password = prefs.getString(KEY_PASSWORD, "") ?: "",
            baseTopic = prefs.getString(KEY_BASE_TOPIC, DEFAULT_BASE_TOPIC) ?: DEFAULT_BASE_TOPIC
        )
    }

    fun saveSettings(settings: MqttSettings) {
        prefs.edit().apply {
            putString(KEY_LOCAL_IP, settings.localIp)
            putString(KEY_REMOTE_IP, settings.remoteIp)
            putString(KEY_PORT, settings.port)
            putString(KEY_USERNAME, settings.username)
            putString(KEY_PASSWORD, settings.password)
            putString(KEY_BASE_TOPIC, settings.baseTopic)
            apply()
        }
    }
}
