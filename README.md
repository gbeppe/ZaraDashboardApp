# 🏠 Zara Dashboard & AI Assistant

Una dashboard domotica avanzata per Android, progettata per il monitoraggio e il controllo totale della casa intelligente, ora potenziata da un assistente ad Intelligenza Artificiale Generativa e integrazione video.

---

## 🚀 Caratteristiche Principali

Il progetto unisce il controllo tradizionale via protocollo **MQTT** con le potenzialità dei modelli **LLM (Gemini 3)** e lo streaming video professionale.

### 📊 HUB Domotico (Dashboard Classica)
*   **Overview**: Landing page con riepilogo sintetico dei dati energetici, ambientali e previsionali.
*   **Home**: Panoramica dettagliata con metriche ambientali, stato energetico (Tesla Powerwall) e controllo luci.
*   **Clima**: Gestione avanzata del termostato, velocità VMC e controllo stufa Palazzetti.
*   **Dati & Analisi**: Grafici multi-serie potenziati (Batteria, Temperature, Humidex, Stato Clima e VMC) con storico 24h e zoom dinamico.
*   **Camera**: Integrazione con **tinyCam Monitor Pro** per lo streaming MJPEG di 6 telecamere con swipe orizzontale fluido.
*   **Log**: Registro eventi completo per il monitoraggio delle attività di sistema.
*   **Setup**: Gestione rapida degli automatismi (Modalità Vacanza, Luci ECO, Sensore Portico, AC Auto, Range Orario).

### 🤖 Zara AI (Assistente Generativo)
*   **Intent Extraction**: Comprensione del linguaggio naturale per comandi e info.
*   **Generative UI**: Widget dinamici creati istantaneamente in locale basati sulle risposte dell'AI.

### 🌐 Connettività
*   **Smart Connection**: Riconoscimento automatico WiFi/4G con switch intelligente degli IP per MQTT e Camera.
*   **Stabilità**: Riconnessione automatica MQTT e monitoraggio persistente dello stato.

---

## 🛠️ Tech Stack

*   **Linguaggio**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **AI Engine**: Google Generative AI SDK (Gemini 3)
*   **Networking**: Eclipse Paho MQTT & WebView (per Video)
*   **Database**: Room (Logging locale)

---

## ⚙️ Configurazione

1.  **MQTT**: Tramite l'icona **Tune** in alto, impostare i parametri del Broker (Locale/Remoto) e il **Base Topic**.
2.  **Camera**: Configurare l'IP del server **tinyCam Monitor Pro** (Locale/Remoto) nella scheda dedicata del menu Configurazione.

---

*Sviluppato con ❤️ per una casa più intelligente e reattiva.*
