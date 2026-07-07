# Documentazione Protocollo MQTT - Zara Dashboard App

Questo documento descrive i topic e i messaggi MQTT utilizzati dall'applicazione per la comunicazione con il sistema domotico.

## Configurazione di Base
Il topic base predefinito è: `zara/android/domotica`.  
`{baseTopic}` si riferisce al valore personalizzato nelle impostazioni.

---

## Messaggi in Entrata (Sottoscrizione)
L'app si sottoscrive a `{baseTopic}/#` e processa i seguenti topic:

### Ambiente e Clima
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../env/tempLiving` | Float | Temperatura Soggiorno (°C) |
| `.../env/tempBedroom` | Float | Temperatura Camera da Letto (°C) |
| `.../env/humidexLiving` | Float | Indice Humidex Soggiorno |
| `.../env/humidexBedroom`| Float | Indice Humidex Camera |
| `.../thermostat/living/target/state` | Int | Temperatura target impostata (°C) |

### Stato Sistema (Automatismi)
| Topic | Messaggio | Descrizione |
| :--- | :--- | :--- |
| `.../system/holiday/state` | `1`/`0` | Stato modalità vacanza |
| `.../system/luci_eco/state` | `1`/`0` | Stato luci ECO |
| `.../system/ac_auto/state` | `1`/`0` | Stato modalità automatica clima |
| `.../system/time_range/state` | `H-H` (es `8-16`) | Range orario operativo |

### Telemetria AI (JSON Completo)
- **Topic**: `casa/clima/stato_completo`
- Contiene metriche energetiche, ambientali (identificazione stanza automatica) e previsioni solari/batteria.

---

## Messaggi in Uscita (Pubblicazione)

| Funzione | Topic | Messaggio |
| :--- | :--- | :--- |
| **Sistema Globale (AI)** | `{baseTopic}/system/enabled/set` | `1`/`0` |
| **Modalità Vacanza** | `{baseTopic}/system/holiday/set` | `1`/`0` |
| **AC Auto** | `{baseTopic}/system/ac_auto/set` | `1`/`0` |
| **Range Orario** | `{baseTopic}/system/time_range/set` | `H-H` (es. `8-16`) |
| **Target Termostato**| `{baseTopic}/thermostat/living/target/set` | Int (es. `24`) |
| **Velocità VMC** | `{baseTopic}/vmc/speed/set` | Int (0-3) |

---

## Integrazione Video (tinyCam)
- Accesso via Web Server tinyCam su porta `8083`.
- URL Streaming: `http://IP:PORT/axis-cgi/mjpg/video.cgi?camera=[ID]`
