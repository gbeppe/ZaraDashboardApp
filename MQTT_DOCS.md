# Documentazione Protocollo MQTT - Zara Dashboard App

Questo documento descrive i topic e i messaggi MQTT utilizzati dall'applicazione Zara Dashboard App per la comunicazione con il sistema domotico.

## Configurazione di Base
Il topic base predefinito è: `zara/android/domotica`.  
Può essere personalizzato nelle impostazioni dell'app. Nelle tabelle seguenti, `{baseTopic}` si riferisce a questo valore.

---

## Messaggi in Entrata (Sottoscrizione)
L'app si sottoscrive a `{baseTopic}/#` e processa i seguenti topic:

### Energia
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../energy/grid` | Float | Potenza scambiata con la rete (W) |
| `.../energy/surplus` | Float | Surplus energetico attuale (W) |
| `.../energy/production` | Float | Produzione fotovoltaica attuale (W) |
| `.../energy/consumption` | Float | Consumo totale dell'abitazione (W) |
| `.../energy/battery` | Float (0-100) | Stato di carica (SoC) della batteria (%) |

### Ambiente e Clima
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../env/tempLiving` | Float | Temperatura Soggiorno (°C) |
| `.../env/humLiving` | Float | Umidità Soggiorno (%) |
| `.../env/humidexLiving` | Float | Indice Humidex Soggiorno |
| `.../env/tempBedroom` | Float | Temperatura Camera da Letto (°C) |
| `.../env/humBedroom` | Float | Umidità Camera da Letto (%) |
| `.../env/humidexBedroom` | Float | Indice Humidex Camera da Letto |
| `.../env/tempOutdoor` | Float | Temperatura Esterna (°C) |
| `.../env/humOutdoor` | Float | Umidità Esterna (%) |
| `.../thermostat/living/target/state` | Int | Temperatura target impostata (°C) |

### Riscaldamento e Puffer
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../acsPufferTemp` | Float | Temperatura accumulo ACS (°C) |
| `.../pufferAltoTemp` | Float | Temperatura Puffer Parte Alta (°C) |
| `.../pufferBassoTemp` | Float | Temperatura Puffer Parte Bassa (°C) |

### Stato Sistema (Automatismi)
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../system/enabled/state` | `1`/`0` o `true`/`false` | Stato globale sistema AI |
| `.../system/holiday/state` | `1`/`0` o `true`/`false` | Stato modalità vacanza |
| `.../system/luci_eco/state` | `1`/`0` o `true`/`false` | Stato luci ECO |
| `.../system/luci_piscina_auto/state` | `1`/`0` o `true`/`false` | Stato luci piscina AUTO |
| `.../system/sensore_portico/state` | `1`/`0` o `true`/`false` | Stato sensore portico |
| `.../system/ac_auto/state` | `1`/`0` o `true`/`false` | Stato modalità automatica clima |
| `.../system/time_range/state` | String (es. `8-16`) | Range orario operativo sistema |

### VMC e Luci
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../vmc/speed/state` | Int (0-3) | Velocità attuale VMC |
| `.../light/{nome}/state` | `ON`/`OFF` | Stato della luce/relè `{nome}` |

### Telemetria AI (JSON)
L'app riceve uno stato completo tramite un payload JSON complesso.
- **Topic**: `casa/clima/stato_completo` (o `{baseTopic}/casa/clima/stato_completo`)
- **Esempio Formato**:
```json
{
  "timestamp": 1690000000000,
  "data_ora_formattata": "2026-07-07 13:18:32",
  "stagione_attiva": "ESTATE",
  "metriche_elettriche": {
    "produzione_fv_w": 4500.5,
    "consumo_casa_w": 1200.0,
    "surplus_w": 3300.5,
    "powerwall_soc_percent": 85.0
  },
  "metriche_ambientali": {
    "temperatura_c": 24.5,
    "temp_cameraMatrimoniale": 22.0,
    "humidex_living": 26.5
  },
  "logica_controllo": {
    "soglia_attivazione_applicata": 1500.0,
    "tempo_mancante_anticiclo_minuti": 5,
    "stanza_rilevamento_vmc": "LIVING",
    "vmc_portata_stimata_m3h": 150,
    "previsione_solare_domani_kwh": 35.5,
    "previsione_solare_data": "2026-07-08"
  },
  "stato_condizionatore": {
    "stato_attuale": "RAFFRESCAMENTO",
    "temperatura_impostata_c": 24.0,
    "modalita_aria": "AUTO"
  },
  "stato_vmc": {
    "velocita_attuale": 2,
    "motivo_logica": "Qualità aria ottimale"
  }
}
```

---

## Integrazione Video (tinyCam Monitor Pro)
L'app può integrare lo streaming video tramite il Web Server di tinyCam Monitor Pro.
- **URL Base**: `http://IP_TINYCAM:PORTA`
- **Streaming MJPEG (Singola Camera)**: `http://IP:PORT/axis-cgi/mjpg/video.cgi?camera=[ID]` (ID inizia da 1)
- **Porta Predefinita**: `8083`

---

## Messaggi in Uscita (Pubblicazione)

| Funzione | Topic | Messaggio |
| :--- | :--- | :--- |
| **Accensione Luce** | `{baseTopic}/light/{nome}/set` | `ON`/`OFF` |
| **Sistema Globale (AI)** | `{baseTopic}/system/enabled/set` | `1` (Attivo), `0` (Disattivo) |
| **Modalità Vacanza** | `{baseTopic}/system/holiday/set` | `1` (Attivo), `0` (Disattivo) |
| **Luci ECO** | `{baseTopic}/system/luci_eco/set` | `1` (Attivo), `0` (Disattivo) |
| **Luci Piscina AUTO** | `{baseTopic}/system/luci_piscina_auto/set` | `1` (Attivo), `0` (Disattivo) |
| **Sensore Portico** | `{baseTopic}/system/sensore_portico/set` | `1` (Attivo), `0` (Disattivo) |
| **AC Auto** | `{baseTopic}/system/ac_auto/set` | `1` (Attivo), `0` (Disattivo) |
| **Range Orario** | `{baseTopic}/system/time_range/set` | String (es. `8-16`) |
| **Target Termostato**| `{baseTopic}/thermostat/living/target/set` | Int (es. `24`) |
| **Velocità VMC** | `{baseTopic}/vmc/speed/set` | Int (es. `2`) |
| **Scena Luci** | `{baseTopic}/scene/set` | String (es. `TV_MODE`) |
