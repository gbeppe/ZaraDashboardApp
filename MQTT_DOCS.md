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
| `.../ac_auto/state` | `ON`/`OFF` o `true`/`false` | Stato modalità automatica clima |

### Riscaldamento e Puffer
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../acsPufferTemp` | Float | Temperatura accumulo ACS (°C) |
| `.../pufferAltoTemp` | Float | Temperatura Puffer Parte Alta (°C) |
| `.../pufferBassoTemp` | Float | Temperatura Puffer Parte Bassa (°C) |

### VMC e Luci
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../vmc/speed/state` | Int (0-3) | Velocità attuale VMC |
| `.../light/{nome}/state` | `ON`/`OFF` | Stato della luce/relè `{nome}` |

### Telemetria AI (JSON)
L'app riceve uno stato completo tramite un payload JSON.
- **Topic**: `casa/clima/stato_completo`
- **Formato**:
```json
{
  "data_ora": "YYYY-MM-DD HH:mm:ss",
  "evento": "DESCRIZIONE_EVENTO",
  "dettaglio_comandi": {
    "motivo_logica": "Spiegazione della logica AI"
  },
  "stato_ac_attuale": "ON/OFF"
}
```

---

## Messaggi in Uscita (Pubblicazione)

| Funzione | Topic | Messaggio |
| :--- | :--- | :--- |
| **Accensione Luce** | `{baseTopic}/light/{nome}/set` | `ON`/`OFF` |
| **Sistema Globale** | `{baseTopic}/system/enabled/set` | `1` (Attivo), `0` (Disattivo) |
| **Modalità Vacanza** | `{baseTopic}/holiday/set` | `1` (Attivo), `0` (Disattivo) |
| **Target Termostato**| `{baseTopic}/thermostat/living/target/set` | Int (es. `24`) |
| **Velocità VMC** | `{baseTopic}/vmc/speed/set` | Int (es. `2`) |
