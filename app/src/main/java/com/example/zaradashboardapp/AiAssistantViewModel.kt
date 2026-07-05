package com.example.zaradashboardapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.defineFunction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private val chat = generativeModel.startChat()

    fun sendCommand(userText: String) {
        viewModelScope.launch {
            try {
                var response = chat.sendMessage(userText)
                
                // Gestione Function Call
                val functionCall = response.functionCalls.firstOrNull()
                if (functionCall != null) {
                    val result = when (functionCall.name) {
                        "get_temperature_data" -> {
                            Log.d("AiAssistant", "Eseguo get_temperature_data")
                            JSONObject().apply {
                                put("status", "ok")
                                put("temperature", 22.5)
                                put("humidity", 45)
                            }
                        }
                        "set_vmc_speed" -> {
                            val speed = functionCall.args["speed"]?.toIntOrNull() ?: 0
                            Log.d("AiAssistant", "Eseguo set_vmc_speed con speed: $speed")
                            JSONObject().apply {
                                put("status", "ok")
                                put("new_speed", speed)
                            }
                        }
                        else -> JSONObject().put("error", "unknown function")
                    }

                    // Invia la risposta della funzione all'LLM
                    response = chat.sendMessage(
                        content("function") {
                            part(FunctionResponsePart(functionCall.name, result))
                        }
                    )
                }

                // Aggiorna lo stato con il testo finale (JSON)
                _uiJsonState.value = response.text ?: ""
                Log.d("AiAssistant", "Risposta finale: ${response.text}")

            } catch (e: Exception) {
                Log.e("AiAssistant", "Errore durante l'invio del comando", e)
                _uiJsonState.value = "{\"error\": \"${e.message}\"}"
            }
        }
    }
}
