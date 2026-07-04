package com.example.zaradashboardapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.LogDao
import com.example.database.DatabaseProvider
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

// --- Data Classes ---

@JsonClass(generateAdapter = true)
data class AiTelemetryPayload(
    @Json(name = "data_ora") val dataOra: String,
    @Json(name = "evento") val evento: String,
    @Json(name = "dettaglio_comandi") val dettaglioComandi: DettaglioComandi,
    @Json(name = "stato_ac_attuale") val statoAcAttuale: String? = null
)

@JsonClass(generateAdapter = true)
data class DettaglioComandi(
    @Json(name = "motivo_logica") val motivoLogica: String
)

enum class ConnectionStatus {
    CONNECTED_LOCAL,
    CONNECTED_REMOTE,
    DISCONNECTED,
    CONNECTING
}

data class LogEvent(
    val id: UUID = UUID.randomUUID(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val eventType: String,
    val message: String,
    val details: String,
    val isSystemEnabled: Boolean
)

data class EnergyMetrics(
    val productionFvW: Int = 0,
    val consumptionHomeW: Int = 0,
    val surplusW: Int = 0,
    val powerwallSoc: Int = 0,
    val gridPowerW: Int = 0
)

data class EnvironmentalMetrics(
    val tempLiving: Float = 0f,
    val humLiving: Float = 0f,
    val humidexLiving: Float = 0f,
    val tempBedroom: Float = 0f,
    val humBedroom: Float = 0f,
    val humidexBedroom: Float = 0f,
    val tempOutdoor: Float = 0f,
    val humOutdoor: Float = 0f,
    val humidexOutdoor: Float = 0f
)

data class ClimateState(
    val isAcOn: Boolean = false,
    val mode: String = "OFF",
    val targetTemp: Int = 24,
    val fanSpeed: String = "AUTO",
    val isAutoModeEnabled: Boolean = true,
    val reason: String = "N/A"
)

data class VmcState(
    val fanSpeed: Int = 0,
    val maxNightSpeed: Int = 0,
    val bypassOpen: Boolean = false,
    val isAutoModeEnabled: Boolean = true,
    val reason: String = "N/A"
)

data class HeatingState(
    val acsBufferTemp: Float = 0f,
    val highBufferTemp: Float = 0f,
    val lowBufferTemp: Float = 0f
)

data class ControlsState(
    val minOnCompressore: Int = 45,
    val minOffCompressore: Int = 40,
    val sogliaHumidexNotte: Int = 29,
    val velocitaMaxVmcNotte: Float = 1f,
    val tolleranzaDeficit: Int = 20,
    val gestioneAcMattinoSolar: Boolean = false,
    val sogliaEmergenzaHumidex: Int = 30
)

data class SystemState(
    val isGlobalEnabled: Boolean = true,
    val isHolidayMode: Boolean = false,
    val energy: EnergyMetrics = EnergyMetrics(),
    val env: EnvironmentalMetrics = EnvironmentalMetrics(),
    val heating: HeatingState = HeatingState(),
    val climate: ClimateState = ClimateState(),
    val vmc: VmcState = VmcState(),
    val controls: ControlsState = ControlsState(),
    val lights: Map<String, Boolean> = emptyMap(),
    val recentLogs: List<LogEvent> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED
)

// --- ViewModel ---

class DashboardViewModel(
    application: Application,
    private val logDao: LogDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SystemState())
    val uiState: StateFlow<SystemState> = _uiState.asStateFlow()

    private val settingsManager = SettingsManager(application)
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val telemetryAdapter = moshi.adapter(AiTelemetryPayload::class.java)

    private val mqttManager = MqttManager(
        context = application,
        onStatusChanged = { status ->
            _uiState.update { it.copy(connectionStatus = status) }
            addLog("SYSTEM", "Connection Status Changed", "New status: $status")
        },
        onMessageReceived = { topic, message ->
            handleMqttMessage(topic, message)
        }
    )

    init {
        initMqtt()
    }

    /**
     * Carica le impostazioni e inizializza la connessione MQTT.
     */
    fun initMqtt() {
        val settings = settingsManager.getSettings()
        mqttManager.connect(settings)
    }

    /**
     * Restituisce le impostazioni attuali.
     */
    fun getSettings(): SettingsManager.MqttSettings = settingsManager.getSettings()

    /**
     * Salva le nuove impostazioni e riconnette l'MQTT.
     */
    fun saveSettings(settings: SettingsManager.MqttSettings) {
        settingsManager.saveSettings(settings)
        mqttManager.disconnect()
        initMqtt()
        addLog("ACTION", "Settings Saved", "MQTT Reconnecting...")
    }

    /**
     * Aggiorna un parametro di controllo, aggiorna la UI e pubblica su MQTT con retain sui topic reali Node-RED.
     */
    fun updateControl(controlName: String, value: Any) {
        viewModelScope.launch {
            _uiState.update { state ->
                val newControls = when (controlName) {
                    "minOnCompressore" -> state.controls.copy(minOnCompressore = value as Int)
                    "minOffCompressore" -> state.controls.copy(minOffCompressore = value as Int)
                    "sogliaHumidexNotte" -> state.controls.copy(sogliaHumidexNotte = value as Int)
                    "velocitaMaxVmcNotte" -> state.controls.copy(velocitaMaxVmcNotte = value as Float)
                    "tolleranzaDeficit" -> state.controls.copy(tolleranzaDeficit = value as Int)
                    "gestioneAcMattinoSolar" -> state.controls.copy(gestioneAcMattinoSolar = value as Boolean)
                    "sogliaEmergenzaHumidex" -> state.controls.copy(sogliaEmergenzaHumidex = value as Int)
                    else -> state.controls
                }
                state.copy(controls = newControls)
            }

            val mqttTopic = when (controlName) {
                "minOnCompressore" -> "casa/clima/cmnd/min_run_time"
                "minOffCompressore" -> "casa/clima/cmnd/min_off_time"
                "sogliaHumidexNotte" -> "casa/clima/cmnd/target_humidex"
                "velocitaMaxVmcNotte" -> "casa/clima/cmnd/vmc_max_notte"
                "tolleranzaDeficit" -> "casa/clima/cmnd/deficit_tolerance_time"
                "gestioneAcMattinoSolar" -> "casa/clima/cmnd/grace_mode_solar"
                "sogliaEmergenzaHumidex" -> "casa/clima/cmnd/emergency_humidex_away"
                else -> null
            }

            mqttTopic?.let {
                mqttManager.publish(it, value.toString(), retained = true)
                addLog("CONTROL", "Sync Sent", "Topic: $it, Val: $value")
            }
        }
    }

    /**
     * Gestisce i messaggi MQTT in arrivo con mappatura dei topic flat e JSON.
     */
    fun handleMqttMessage(topic: String, message: String) {
        viewModelScope.launch {
            when {
                topic.contains("/energy/grid") -> {
                    val surplus = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(gridPowerW = surplus.toInt())) }
                }
                topic.contains("/energy/surplus") -> {
                    val surplus = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(surplusW = surplus.toInt())) }
                }
                topic.contains("/energy/production") -> {
                    val prod = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(productionFvW = prod.toInt())) }
                }
                topic.contains("/energy/consumption") -> {
                    val cons = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(consumptionHomeW = cons.toInt())) }
                }
                topic.contains("/energy/battery") -> {
                    // Supponendo che la batteria sia in percentuale (es. 85 per 85%)
                    val batt = message.toFloatOrNull()?.toInt() ?: 0
                    _uiState.update { it.copy(energy = it.energy.copy(powerwallSoc = batt)) }
                }
                // JSON Completo Telemetria AI
                topic == "casa/clima/stato_completo" -> {
                    parseAiTelemetry(message)
                }

                // VMC Speed
                topic.endsWith("/vmc/speed/state") -> {
                    val speed = message.toIntOrNull() ?: 0
                    _uiState.update { it.copy(vmc = it.vmc.copy(fanSpeed = speed)) }
                }
                
                // Climate Target
                topic.endsWith("/thermostat/living/target/state") -> {
                    val temp = message.toIntOrNull() ?: 24
                    _uiState.update { it.copy(climate = it.climate.copy(targetTemp = temp)) }
                }
                
                // AC Auto Mode
                topic.endsWith("/ac_auto/state") -> {
                    val isAuto = message == "ON" || message == "true"
                    _uiState.update { it.copy(climate = it.climate.copy(isAutoModeEnabled = isAuto)) }
                }
                
                // Environmental Data
                topic.contains("/env/tempBedroom") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(tempBedroom = valFloat)) }
                }
                topic.contains("/env/humBedroom") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humBedroom = valFloat)) }
                }
                topic.contains("/env/tempOutdoor") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(tempOutdoor = valFloat)) }
                }
                topic.contains("/env/humOutdoor") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humOutdoor = valFloat)) }
                }
                topic.contains("/env/tempLiving") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(tempLiving = valFloat)) }
                }
                topic.contains("/env/humLiving") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humLiving = valFloat)) }
                }
                topic.contains("/env/humidexLiving") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humidexLiving = valFloat)) }
                }
                topic.contains("/env/humidexBedroom") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humidexBedroom = valFloat)) }
                }

                // Heating / Puffer Data
                topic.contains("/acsPufferTemp") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(heating = it.heating.copy(acsBufferTemp = valFloat)) }
                }
                topic.contains("/pufferAltoTemp") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(heating = it.heating.copy(highBufferTemp = valFloat)) }
                }
                topic.contains("/pufferBassoTemp") -> {
                    val valFloat = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(heating = it.heating.copy(lowBufferTemp = valFloat)) }
                }

                // Node-RED Sync (Back-sync from cmnd/stat topics)
                topic.contains("min_run_time") -> {
                    val value = message.toFloatOrNull()?.toInt() ?: 45
                    _uiState.update { it.copy(controls = it.controls.copy(minOnCompressore = value)) }
                }
                topic.contains("min_off_time") -> {
                    val value = message.toFloatOrNull()?.toInt() ?: 40
                    _uiState.update { it.copy(controls = it.controls.copy(minOffCompressore = value)) }
                }
                topic.contains("target_humidex") -> {
                    val value = message.toFloatOrNull()?.toInt() ?: 29
                    _uiState.update { it.copy(controls = it.controls.copy(sogliaHumidexNotte = value)) }
                }
                topic.contains("vmc_max_notte") -> {
                    val value = message.toFloatOrNull() ?: 1f
                    _uiState.update { it.copy(controls = it.controls.copy(velocitaMaxVmcNotte = value)) }
                }
                topic.contains("deficit_tolerance_time") -> {
                    val value = message.toFloatOrNull()?.toInt() ?: 20
                    _uiState.update { it.copy(controls = it.controls.copy(tolleranzaDeficit = value)) }
                }
                topic.contains("grace_mode_solar") -> {
                    val value = message.lowercase() == "true" || message == "1"
                    _uiState.update { it.copy(controls = it.controls.copy(gestioneAcMattinoSolar = value)) }
                }
                topic.contains("emergency_humidex_away") -> {
                    val value = message.toFloatOrNull()?.toInt() ?: 30
                    _uiState.update { it.copy(controls = it.controls.copy(sogliaEmergenzaHumidex = value)) }
                }
                
                // Lights and Relays
                topic.contains("/light/") && topic.endsWith("/state") -> {
                    val lightName = topic.split("/").dropLast(1).last()
                    val isOn = message == "ON"
                    _uiState.update { 
                        val updatedLights = it.lights.toMutableMap()
                        updatedLights[lightName] = isOn
                        it.copy(lights = updatedLights)
                    }
                }
            }
            
            addLog("MQTT", "Data Update", "Topic: $topic, Val: $message")
        }
    }

    // --- User Actions ---

    /**
     * Imposta lo stato di una luce o relè.
     */
    fun setLightState(lightName: String, turnOn: Boolean) {
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (turnOn) "ON" else "OFF"
            mqttManager.publish("${settings.baseTopic}/light/$lightName/set", payload)
            
            // Ottimizzazione UI: aggiorniamo lo stato locale immediatamente per reattività
            _uiState.update { 
                val updatedLights = it.lights.toMutableMap()
                updatedLights[lightName] = turnOn
                it.copy(lights = updatedLights)
            }
            addLog("ACTION", "Light Toggled", "$lightName -> $payload")
        }
    }

    /**
     * Abilita/Disabilita il sistema globale.
     */
    fun toggleSystem() {
        val newState = !_uiState.value.isGlobalEnabled
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (newState) "1" else "0"
            mqttManager.publish("${settings.baseTopic}/system/enabled/set", payload)
            
            _uiState.update { it.copy(isGlobalEnabled = newState) }
            addLog("ACTION", "Global System Toggled", "New state: $newState (Payload: $payload)")
        }
    }

    /**
     * Abilita/Disabilita la modalità vacanza.
     */
    fun toggleHolidayMode() {
        val newState = !_uiState.value.isHolidayMode
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (newState) "1" else "0"
            mqttManager.publish("${settings.baseTopic}/holiday/set", payload)
            
            _uiState.update { it.copy(isHolidayMode = newState) }
            addLog("ACTION", "Holiday Mode Toggled", "New state: $newState (Payload: $payload)")
        }
    }

    /**
     * Imposta la temperatura target del clima.
     */
    fun setClimateTarget(temp: Int) {
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            mqttManager.publish("${settings.baseTopic}/thermostat/living/target/set", temp.toString())
            
            _uiState.update { it.copy(climate = it.climate.copy(targetTemp = temp)) }
            addLog("ACTION", "Climate Target Set", "Target: $temp°C")
        }
    }

    /**
     * Imposta la velocità della VMC.
     */
    fun setVmcSpeed(speed: Int) {
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            mqttManager.publish("${settings.baseTopic}/vmc/speed/set", speed.toString())
            
            _uiState.update { it.copy(vmc = it.vmc.copy(fanSpeed = speed)) }
            addLog("ACTION", "VMC Speed Set", "Speed: $speed")
        }
    }

    // --- Private Helpers ---

    /**
     * Aggiunge un log locale allo stato, mantenendo solo gli ultimi 20 log.
     */
    private fun addLog(type: String, message: String, details: String) {
        val newLog = LogEvent(
            eventType = type,
            message = message,
            details = details,
            isSystemEnabled = _uiState.value.isGlobalEnabled
        )
        
        _uiState.update { currentState ->
            val updatedLogs = (listOf(newLog) + currentState.recentLogs).take(20)
            currentState.copy(recentLogs = updatedLogs)
        }
    }

    private suspend fun parseAiTelemetry(json: String) {
        withContext(Dispatchers.Default) {
            try {
                val payload = telemetryAdapter.fromJson(json)
                payload?.let { data ->
                    _uiState.update { state ->
                        state.copy(
                            climate = state.climate.copy(
                                isAcOn = data.statoAcAttuale == "ON",
                                reason = data.dettaglioComandi.motivoLogica
                            ),
                            vmc = state.vmc.copy(
                                reason = data.dettaglioComandi.motivoLogica
                            )
                        )
                    }
                    
                    // Aggiungiamo un log per l'evento ricevuto
                    val timePrefix = data.dataOra.split(" ").lastOrNull()?.take(5) ?: ""
                    addLog(
                        type = "AI_ENGINE",
                        message = data.evento.replace("_", " "),
                        details = "[$timePrefix] ${data.dettaglioComandi.motivoLogica}"
                    )
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error parsing AI telemetry", e)
                addLog("ERROR", "JSON Parsing Failed", e.message ?: "Unknown error")
            }
        }
    }
}
