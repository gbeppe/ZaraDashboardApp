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
    @Json(name = "timestamp") val timestamp: Long? = null,
    @Json(name = "data_ora_formattata") val dataOra: String? = null,
    @Json(name = "stagione_attiva") val stagioneAttiva: String? = null,
    @Json(name = "metriche_elettriche") val metricheElettriche: MetricheElettriche? = null,
    @Json(name = "metriche_ambientali") val ambienti: AmbientiData? = null,
    @Json(name = "logica_controllo") val logicaControllo: LogicaControllo? = null,
    @Json(name = "stato_condizionatore") val clima: ClimaData? = null,
    @Json(name = "stato_vmc") val vmc: VmcData? = null
)

@JsonClass(generateAdapter = true)
data class MetricheElettriche(
    @Json(name = "produzione_fv_w") val produzioneFvW: Float? = null,
    @Json(name = "consumo_casa_w") val consumoCasaW: Float? = null,
    @Json(name = "surplus_w") val surplusW: Float? = null,
    @Json(name = "powerwall_soc_percent") val powerwallSocPercent: Float? = null,
    @Json(name = "consumo_ac_w") val consumoAcW: Float? = null
)

@JsonClass(generateAdapter = true)
data class LogicaControllo(
    @Json(name = "soglia_attivazione_applicata") val sogliaAttivazione: Float? = null,
    @Json(name = "tempo_mancante_anticiclo_minuti") val tempoAnticiclo: Int? = null,
    @Json(name = "stanza_rilevamento_vmc") val stanzaVmc: String? = null,
    @Json(name = "vmc_portata_stimata_m3h") val portataVmc: Int? = null,
    @Json(name = "previsione_solare_domani_kwh") val previsioneSolareKwh: Float? = null,
    @Json(name = "previsione_solare_data") val previsioneSolareData: String? = null,
    @Json(name = "previsione_ricarica_batteria_percent") val previsioneBatteriaPercent: Float? = null,
    @Json(name = "kwh_stimati_in_batteria") val kwhStimatiBatteria: Float? = null
)

@JsonClass(generateAdapter = true)
data class ClimaData(
    @Json(name = "stato_attuale") val statoAttuale: String? = null,
    @Json(name = "temperatura_impostata_c") val targetTemp: Float? = null,
    @Json(name = "modalita_aria") val modalitaAria: String? = null
)

@JsonClass(generateAdapter = true)
data class VmcData(
    @Json(name = "velocita_attuale") val fanSpeed: Int? = null,
    @Json(name = "motivo_logica") val motivoLogica: String? = null
)

@JsonClass(generateAdapter = true)
data class AmbientiData(
    @Json(name = "temperatura_c") val tempLiving: Float? = null,
    @Json(name = "humidex_living") val humidexLiving: Float? = null,
    @Json(name = "temp_cameraMatrimoniale") val tempBedroom: Float? = null,
    @Json(name = "humidex_bedroom") val humidexBedroom: Float? = null
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
) {
    val allTemperatures: Map<String, Double>
        get() = mapOf(
            "Soggiorno" to tempLiving.toDouble(),
            "Camera da Letto" to tempBedroom.toDouble(),
            "Esterno" to tempOutdoor.toDouble()
        ).filterValues { it != 0.0 }
}

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
    val sogliaEmergenzaHumidex: Float = 30.0f
)

data class StoveState(
    val acceso: Boolean = false,
    val modalita: String = "sconosciuta",
    val potenza: Int = 1
)

data class LogicaControlloState(
    val tempoAnticiclo: Int = 0,
    val stanzaVmc: String = "--",
    val portataVmc: Int = 0,
    val previsioneSolareKwh: Float = 0f,
    val previsioneSolareData: String = "",
    val previsioneBatteriaPercent: Float = 0f,
    val kwhStimatiBatteria: Float = 0f
)

data class SystemState(
    val isGlobalEnabled: Boolean = true,
    val isHolidayMode: Boolean = false,
    val isLuciEcoEnabled: Boolean = false,
    val isLuciPiscinaAutoEnabled: Boolean = false,
    val isSensorePorticoEnabled: Boolean = true,
    val timeRangeStart: Int = 8,
    val timeRangeEnd: Int = 16,
    val lastUpdateTime: String = "",
    val activeSeason: String = "--",
    val waitingForData: Boolean = true,
    val energy: EnergyMetrics = EnergyMetrics(),
    val env: EnvironmentalMetrics = EnvironmentalMetrics(),
    val logic: LogicaControlloState = LogicaControlloState(),
    val heating: HeatingState = HeatingState(),
    val climate: ClimateState = ClimateState(),
    val vmc: VmcState = VmcState(),
    val controls: ControlsState = ControlsState(),
    val stove: StoveState = StoveState(),
    val lights: Map<String, Boolean> = emptyMap(),
    val recentLogs: List<LogEvent> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val batteryHistory: List<Float> = emptyList(),
    val humidexHistory: List<Float> = emptyList(),
    val surplusHistory: List<Float> = emptyList(),
    val tempLivingHistory: List<Float> = emptyList(),
    val tempBedroomHistory: List<Float> = emptyList(),
    val tempOutdoorHistory: List<Float> = emptyList(),
    val humidexBedroomHistory: List<Float> = emptyList()
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
            Log.d("MQTT_DEBUG", "Status: $status")
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
        Log.d("MQTT_DEBUG", "initMqtt called")
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
                    "sogliaEmergenzaHumidex" -> state.controls.copy(sogliaEmergenzaHumidex = value as Float)
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
        Log.d("MQTT_DEBUG", "Received: $topic -> $message")
        val baseTopic = settingsManager.getSettings().baseTopic
        viewModelScope.launch {
            when {
                topic.contains("/energy/grid") -> {
                    val surplus = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(gridPowerW = surplus.toInt())) }
                }
                topic.contains("/energy/surplus") -> {
                    val surplus = message.toFloatOrNull() ?: 0f
                    _uiState.update { 
                        it.copy(
                            energy = it.energy.copy(surplusW = surplus.toInt()),
                            surplusHistory = it.surplusHistory.appendWithLimit(surplus)
                        )
                    }
                }
                topic.contains("/energy/production") -> {
                    val prod = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(productionFvW = prod.toInt())) }
                }
                topic.contains("/energy/consumption") -> {
                    val cons = message.toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(energy = it.energy.copy(consumptionHomeW = cons.toInt())) }
                }
                topic.contains("$baseTopic/casa/clima/stato_completo/metriche_elettriche.powerwall_soc_percent") -> {
                    // Supponendo che la batteria sia in percentuale (es. 85 per 85%)
                    val batt = message.toFloatOrNull()?.toInt() ?: 0
                    _uiState.update { 
                        it.copy(
                            energy = it.energy.copy(powerwallSoc = batt),
                            batteryHistory = it.batteryHistory.appendWithLimit(batt.toFloat())
                        )
                    }
                }
                // JSON Completo Telemetria AI
                topic == "$baseTopic/casa/clima/stato_completo" -> {
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
                topic.endsWith("/system/ac_auto/state") -> {
                    val isAuto = message == "1" || message == "true" || message == "ON"
                    _uiState.update { it.copy(climate = it.climate.copy(isAutoModeEnabled = isAuto)) }
                }

                // New System States
                topic.endsWith("/system/luci_eco/state") -> {
                    val isEnabled = message == "1" || message == "true" || message == "ON"
                    _uiState.update { it.copy(isLuciEcoEnabled = isEnabled) }
                }
                topic.endsWith("/system/luci_piscina_auto/state") -> {
                    val isEnabled = message == "1" || message == "true" || message == "ON"
                    _uiState.update { it.copy(isLuciPiscinaAutoEnabled = isEnabled) }
                }
                topic.endsWith("/system/sensore_portico/state") -> {
                    val isEnabled = message == "1" || message == "true" || message == "ON"
                    _uiState.update { it.copy(isSensorePorticoEnabled = isEnabled) }
                }
                topic.endsWith("/system/holiday/state") -> {
                    val isEnabled = message == "1" || message == "true" || message == "ON"
                    _uiState.update { it.copy(isHolidayMode = isEnabled) }
                }

                topic.endsWith("/system/time_range/state") -> {
                    val parts = message.split("-")
                    if (parts.size == 2) {
                        val start = parts[0].trim().toIntOrNull() ?: 8
                        val end = parts[1].trim().toIntOrNull() ?: 16
                        _uiState.update { it.copy(timeRangeStart = start, timeRangeEnd = end) }
                    }
                }
                
                // Environmental Data
                topic.contains("/env/tempBedroom") || topic.endsWith("/env/tempBedroom") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    Log.d("MQTT_DEBUG", "Parsed Bedroom Temp: $valFloat")
                    _uiState.update { 
                        it.copy(
                            env = it.env.copy(tempBedroom = valFloat),
                            tempBedroomHistory = it.tempBedroomHistory.appendWithLimit(valFloat)
                        )
                    }
                }
                topic.contains("/env/humBedroom") || topic.endsWith("/env/humBedroom") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humBedroom = valFloat)) }
                }
                topic.contains("/env/tempOutdoor") || topic.endsWith("/env/tempOutdoor") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    Log.d("MQTT_DEBUG", "Parsed Outdoor Temp: $valFloat")
                    _uiState.update { 
                        it.copy(
                            env = it.env.copy(tempOutdoor = valFloat),
                            tempOutdoorHistory = it.tempOutdoorHistory.appendWithLimit(valFloat)
                        )
                    }
                }
                topic.contains("/env/humOutdoor") || topic.endsWith("/env/humOutdoor") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humOutdoor = valFloat)) }
                }
                topic.contains("/env/tempLiving") || topic.endsWith("/env/tempLiving") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    Log.d("MQTT_DEBUG", "Parsed Living Temp: $valFloat")
                    _uiState.update { 
                        it.copy(
                            env = it.env.copy(tempLiving = valFloat),
                            tempLivingHistory = it.tempLivingHistory.appendWithLimit(valFloat)
                        )
                    }
                }
                topic.contains("/env/humLiving") || topic.endsWith("/env/humLiving") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(env = it.env.copy(humLiving = valFloat)) }
                }
                topic.contains("/env/humidexLiving") || topic.endsWith("/env/humidexLiving") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { 
                        it.copy(
                            env = it.env.copy(humidexLiving = valFloat),
                            humidexHistory = it.humidexHistory.appendWithLimit(valFloat)
                        )
                    }
                }
                topic.contains("/env/humidexBedroom") || topic.endsWith("/env/humidexBedroom") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { 
                        it.copy(
                            env = it.env.copy(humidexBedroom = valFloat),
                            humidexBedroomHistory = it.humidexBedroomHistory.appendWithLimit(valFloat)
                        )
                    }
                }

                // Heating / Puffer Data
                topic.contains("/acsPufferTemp") || topic.endsWith("/acsPufferTemp") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(heating = it.heating.copy(acsBufferTemp = valFloat)) }
                }
                topic.contains("/pufferAltoTemp") || topic.endsWith("/pufferAltoTemp") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
                    _uiState.update { it.copy(heating = it.heating.copy(highBufferTemp = valFloat)) }
                }
                topic.contains("/pufferBassoTemp") || topic.endsWith("/pufferBassoTemp") -> {
                    val valFloat = message.trim().toFloatOrNull() ?: 0f
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
                    val value = message.toFloatOrNull() ?: 30.0f
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

                // Stove Data
                topic.endsWith("casa/stufa/stat/acceso") -> {
                    val isOn = message == "on" || message == "true" || message == "1"
                    _uiState.update { it.copy(stove = it.stove.copy(acceso = isOn)) }
                }
                topic.endsWith("casa/stufa/stat/modalita") -> {
                    _uiState.update { it.copy(stove = it.stove.copy(modalita = message.lowercase())) }
                }
                topic.endsWith("casa/stufa/stat/potenza") -> {
                    val level = message.toIntOrNull() ?: 1
                    _uiState.update { it.copy(stove = it.stove.copy(potenza = level)) }
                }
            }
            
            addLog("MQTT", "Data Update", "Topic: $topic, Val: $message")
            
            // Aggiorna l'orario dell'ultimo messaggio ricevuto
            val now = java.time.LocalDateTime.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")
            _uiState.update { it.copy(lastUpdateTime = now.format(formatter)) }
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
            mqttManager.publish("${settings.baseTopic}/system/holiday/set", payload)
            
            _uiState.update { it.copy(isHolidayMode = newState) }
            addLog("ACTION", "Holiday Mode Toggled", "New state: $newState (Payload: $payload)")
        }
    }

    /**
     * Abilita/Disabilita l'accensione luci ECO.
     */
    fun toggleLuciEco() {
        val newState = !_uiState.value.isLuciEcoEnabled
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (newState) "1" else "0"
            mqttManager.publish("${settings.baseTopic}/system/luci_eco/set", payload)
            _uiState.update { it.copy(isLuciEcoEnabled = newState) }
            addLog("ACTION", "Luci ECO Toggled", "New state: $newState")
        }
    }

    /**
     * Abilita/Disabilita luci piscina AUTO.
     */
    fun toggleLuciPiscinaAuto() {
        val newState = !_uiState.value.isLuciPiscinaAutoEnabled
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (newState) "1" else "0"
            mqttManager.publish("${settings.baseTopic}/system/luci_piscina_auto/set", payload)
            _uiState.update { it.copy(isLuciPiscinaAutoEnabled = newState) }
            addLog("ACTION", "Piscina AUTO Toggled", "New state: $newState")
        }
    }

    /**
     * Abilita/Disabilita sensore portico.
     */
    fun toggleSensorePortico() {
        val newState = !_uiState.value.isSensorePorticoEnabled
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (newState) "1" else "0"
            mqttManager.publish("${settings.baseTopic}/system/sensore_portico/set", payload)
            _uiState.update { it.copy(isSensorePorticoEnabled = newState) }
            addLog("ACTION", "Sensore Portico Toggled", "New state: $newState")
        }
    }

    /**
     * Abilita/Disabilita AC Auto.
     */
    fun toggleAcAuto() {
        val newState = !_uiState.value.climate.isAutoModeEnabled
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = if (newState) "1" else "0"
            mqttManager.publish("${settings.baseTopic}/system/ac_auto/set", payload)
            _uiState.update { it.copy(climate = it.climate.copy(isAutoModeEnabled = newState)) }
            addLog("ACTION", "AC Auto Toggled", "New state: $newState")
        }
    }

    /**
     * Imposta il range di orario del sistema.
     */
    fun setTimeRange(start: Int, end: Int) {
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            val payload = "$start-$end"
            mqttManager.publish("${settings.baseTopic}/system/time_range/set", payload)
            _uiState.update { it.copy(timeRangeStart = start, timeRangeEnd = end) }
            addLog("ACTION", "Time Range Set", "Range: $payload")
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

    /**
     * Imposta una scena di illuminazione.
     */
    fun setLightingScene(scenePayload: String) {
        viewModelScope.launch {
            val settings = settingsManager.getSettings()
            mqttManager.publish("${settings.baseTopic}/scene/set", scenePayload)
            addLog("ACTION", "Lighting Scene", "Set to: $scenePayload")
        }
    }

    /**
     * Gestione comandi Stufa/Caminetto Palazzetti
     */
    fun setStovePower(isOn: Boolean) {
        viewModelScope.launch {
            val payload = if (isOn) "on" else "off"
            mqttManager.publish("${getSettings().baseTopic}/casa/stufa/cmnd/power", payload)
            _uiState.update { it.copy(stove = it.stove.copy(acceso = isOn)) }
            addLog("ACTION", "Caminetto", "Power: $payload")
        }
    }

    fun setStoveMode(mode: String) {
        viewModelScope.launch {
            mqttManager.publish("${getSettings().baseTopic}/casa/stufa/cmnd/modalita", mode)
            _uiState.update { it.copy(stove = it.stove.copy(modalita = mode)) }
            addLog("ACTION", "Caminetto", "Modalità: $mode")
        }
    }

    fun setStoveLevel(level: Int) {
        viewModelScope.launch {
            mqttManager.publish("${getSettings().baseTopic}/casa/stufa/cmnd/potenza", level.toString())
            _uiState.update { it.copy(stove = it.stove.copy(potenza = level)) }
            addLog("ACTION", "Caminetto", "Potenza: $level")
        }
    }

    /**
     * Invia un comando alla stufa (obsoleto - sostituito dai comandi specifici sopra).
     */
    fun sendStoveCommand(button: String) {
        viewModelScope.launch {
            mqttManager.publish("casa/stufa/cmd/pulsante", button, retained = false)
            addLog("ACTION", "Stove Command", "Sent button: $button")
        }
    }

    // --- Private Helpers ---

    /**
     * Aggiunge un log locale allo stato, mantenendo solo gli ultimi 20 log.
     * I nuovi log vengono aggiunti in fondo per evitare salti della lista durante lo scroll.
     */
    private fun addLog(type: String, message: String, details: String) {
        val newLog = LogEvent(
            eventType = type,
            message = message,
            details = details,
            isSystemEnabled = _uiState.value.isGlobalEnabled
        )
        
        _uiState.update { currentState ->
            val updatedLogs = (currentState.recentLogs + listOf(newLog)).takeLast(20)
            currentState.copy(recentLogs = updatedLogs)
        }
    }

    private suspend fun parseAiTelemetry(json: String) {
        withContext(Dispatchers.Default) {
            try {
                val payload = telemetryAdapter.fromJson(json)
                payload?.let { data ->
                    _uiState.update { state ->
                        var newState = state

                        // 1. Metriche Elettriche
                        data.metricheElettriche?.let { metrics ->
                            val currentEnergy = newState.energy.copy(
                                productionFvW = metrics.produzioneFvW?.toInt() ?: newState.energy.productionFvW,
                                consumptionHomeW = metrics.consumoCasaW?.toInt() ?: newState.energy.consumptionHomeW,
                                surplusW = metrics.surplusW?.toInt() ?: newState.energy.surplusW,
                                powerwallSoc = metrics.powerwallSocPercent?.toInt() ?: newState.energy.powerwallSoc
                            )
                            
                            var newBatteryHistory = newState.batteryHistory
                            metrics.powerwallSocPercent?.let { soc ->
                                newBatteryHistory = newBatteryHistory.appendWithLimit(soc)
                            }
                            
                            var newSurplusHistory = newState.surplusHistory
                            metrics.surplusW?.let { surplus ->
                                newSurplusHistory = newSurplusHistory.appendWithLimit(surplus)
                            }

                            newState = newState.copy(
                                energy = currentEnergy,
                                batteryHistory = newBatteryHistory,
                                surplusHistory = newSurplusHistory,
                                waitingForData = false // Abbiamo ricevuto dati validi
                            )
                        }

                        // 2. Clima
                        data.clima?.let { clima ->
                            newState = newState.copy(
                                climate = newState.climate.copy(
                                    isAcOn = clima.statoAttuale != "OFF",
                                    mode = clima.modalitaAria ?: newState.climate.mode,
                                    targetTemp = clima.targetTemp?.toInt() ?: newState.climate.targetTemp
                                )
                            )
                        }

                        // 3. VMC
                        data.vmc?.let { vmc ->
                            newState = newState.copy(
                                vmc = newState.vmc.copy(
                                    fanSpeed = vmc.fanSpeed ?: newState.vmc.fanSpeed,
                                    reason = vmc.motivoLogica ?: newState.vmc.reason
                                )
                            )
                        }

                        // 4. Ambienti
                        data.ambienti?.let { env ->
                            val currentEnv = newState.env.copy(
                                tempLiving = env.tempLiving ?: newState.env.tempLiving,
                                humidexLiving = env.humidexLiving ?: newState.env.humidexLiving,
                                tempBedroom = env.tempBedroom ?: newState.env.tempBedroom,
                                humidexBedroom = env.humidexBedroom ?: newState.env.humidexBedroom
                            )
                            
                            var newHumidexHistory = newState.humidexHistory
                            env.humidexLiving?.let { humidex ->
                                newHumidexHistory = newHumidexHistory.appendWithLimit(humidex)
                            }

                            var newHumidexBedroomHistory = newState.humidexBedroomHistory
                            env.humidexBedroom?.let { humidex ->
                                newHumidexBedroomHistory = newHumidexBedroomHistory.appendWithLimit(humidex)
                            }

                            var newTempLivingHistory = newState.tempLivingHistory
                            env.tempLiving?.let { temp ->
                                newTempLivingHistory = newTempLivingHistory.appendWithLimit(temp)
                            }

                            var newTempBedroomHistory = newState.tempBedroomHistory
                            env.tempBedroom?.let { temp ->
                                newTempBedroomHistory = newTempBedroomHistory.appendWithLimit(temp)
                            }

                            newState = newState.copy(
                                env = currentEnv,
                                humidexHistory = newHumidexHistory,
                                humidexBedroomHistory = newHumidexBedroomHistory,
                                tempLivingHistory = newTempLivingHistory,
                                tempBedroomHistory = newTempBedroomHistory
                            )
                        }

                        // 5. Logica Controllo (Dati aggiuntivi per Overview)
                        data.logicaControllo?.let { logicData ->
                            newState = newState.copy(
                                activeSeason = data.stagioneAttiva ?: newState.activeSeason,
                                lastUpdateTime = data.dataOra ?: newState.lastUpdateTime,
                                logic = newState.logic.copy(
                                    tempoAnticiclo = logicData.tempoAnticiclo ?: newState.logic.tempoAnticiclo,
                                    stanzaVmc = logicData.stanzaVmc ?: newState.logic.stanzaVmc,
                                    portataVmc = logicData.portataVmc ?: newState.logic.portataVmc,
                                    previsioneSolareKwh = logicData.previsioneSolareKwh ?: newState.logic.previsioneSolareKwh,
                                    previsioneSolareData = logicData.previsioneSolareData ?: newState.logic.previsioneSolareData,
                                    previsioneBatteriaPercent = logicData.previsioneBatteriaPercent ?: newState.logic.previsioneBatteriaPercent,
                                    kwhStimatiBatteria = logicData.kwhStimatiBatteria ?: newState.logic.kwhStimatiBatteria
                                )
                            )
                        }

                        newState
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error parsing AI telemetry", e)
                addLog("ERROR", "JSON Parsing Failed", e.message ?: "Unknown error")
            }
        }
    }

    private fun <T> List<T>.appendWithLimit(item: T, limit: Int = 100): List<T> {
        val newList = this.toMutableList()
        newList.add(item)
        if (newList.size > limit) {
            newList.removeAt(0)
        }
        return newList
    }
}
