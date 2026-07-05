package com.example.zaradashboardapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.defineFunction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AiAssistantViewModel : ViewModel() {

    private val _uiJsonState = MutableStateFlow("")
    val uiJsonState: StateFlow<String> = _uiJsonState.asStateFlow()

    private val getTemperatureData = defineFunction(
        name = "get_temperature_data",
        description = "Get the current temperature data for the home."
    )

    private val setVmcSpeed = defineFunction(
        name = "set_vmc_speed",
        description = "Set the VMC fan speed.",
        parameters = listOf(
            Schema.int("speed", "The fan speed level (0-3).")
        )
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = "AQ.Ab8RN6JSqj8hVdiBS6F89dtGrt8RB6DO-gRUXQP0RXe5CK4tvQ",
        tools = listOf(Tool(listOf(getTemperatureData, setVmcSpeed))),
        systemInstruction = content {
            text("Sei un assistente domotico avanzato. Rispondi SEMPRE E SOLO con un oggetto JSON valido. " +
                    "Il JSON deve avere un campo 'ui_type' che può essere 'temperature_card' o 'vmc_card' " +
                    "e i relativi dati necessari per visualizzare il widget richiesto.")
        }
    )

    fun sendCommand(userText: String, temperatureMap: Map<String, Double>) {
        viewModelScope.launch {
            try {
                // 1. Chiediamo all'AI di analizzare il testo in modo stateless
                val response = generativeModel.generateContent(userText)

                // 2. Intercettiamo l'intenzione (la funzione scelta)
                response.functionCalls.firstOrNull()?.let { call ->
                    when (call.name) {
                        "get_temperature_data" -> {
                            val jsonCard = JSONObject()
                            jsonCard.put("ui_type", "temperature_list_card")
                            
                            val dataArray = JSONArray()
                            temperatureMap.forEach { (room, value) ->
                                val item = JSONObject()
                                // Formatta il nome con la prima lettera maiuscola
                                item.put("name", room.replaceFirstChar { it.uppercase() })
                                item.put("value", value)
                                dataArray.put(item)
                            }
                            jsonCard.put("sensors", dataArray)

                            _uiJsonState.value = jsonCard.toString()
                        }
                        "set_vmc_speed" -> {
                            // L'AI ha capito il comando e ha estratto il parametro
                            // Estraiamo l'argomento (l'SDK restituisce una Map)
                            val speedParam = call.args["speed"] ?: "0"
                            val speed = speedParam.toString().toFloatOrNull()?.toInt() ?: 0
                            
                            // (Qui invieremmo il comando MQTT reale: mqttManager.publish(...))
                            
                            // Costruiamo la UI di conferma
                            val jsonResponse = """
                                {
                                  "ui_type": "vmc_card",
                                  "speed": $speed,
                                  "status": "Velocità impostata a $speed"
                                }
                            """.trimIndent()
                            _uiJsonState.value = jsonResponse
                        }
                        else -> {
                            _uiJsonState.value = """{"error": "Funzione sconosciuta chiamata dall'AI."}"""
                        }
                    }
                } ?: run {
                    // Se l'AI non chiama nessuna funzione (es. chiacchiera normale)
                    _uiJsonState.value = """{"error": "L'AI non ha identificato un comando domotico."}"""
                }
            } catch (e: Exception) {
                _uiJsonState.value = """{"error": "${e.localizedMessage}"}"""
            }
        }
    }
}
