# 🏠 Zara Dashboard & AI Assistant

Una dashboard domotica avanzata per Android, progettata per il monitoraggio e il controllo totale della casa intelligente, ora potenziata da un assistente ad Intelligenza Artificiale Generativa.

---

## 🚀 Caratteristiche Principali

Il progetto unisce il controllo tradizionale via protocollo **MQTT** con le potenzialità dei modelli **LLM (Gemini 3)** per un'esperienza d'uso moderna e intuitiva.

### 📊 HUB Domotico (Dashboard Classica)
*   **Overview**: Nuova schermata di atterraggio con riepilogo sintetico dei dati energetici, ambientali e stati operativi della logica di controllo.
*   **Home**: Panoramica dettagliata con metriche ambientali, stato energetico (Tesla Powerwall) e controllo luci.
*   **Clima**: Gestione avanzata del termostato, velocità VMC (Ventilazione Meccanica) e controllo stufa/caminetto Palazzetti.
*   **Dati & Analisi**: Grafici multi-serie storici per batteria, temperature (Living, Camera, Esterno) e indici Humidex con zoom automatico e indicatori live.
*   **Log**: Registro eventi completo e dedicato per il monitoraggio delle attività di sistema.
*   **Setup**: Gestione rapida degli automatismi (Modalità Vacanza, Luci ECO, Sensore Portico, AC Auto).

### 🤖 Zara AI (Assistente Generativo)
*   **Intent Extraction**: L'AI comprende il linguaggio naturale per identificare comandi domotici o richieste di informazioni.
*   **Generative UI**: L'interfaccia si adatta dinamicamente alla risposta dell'AI, mostrando widget specifici (es. schede sensori, controlli VMC) creati istantaneamente in locale.
*   **Integrazione Dati Reali**: L'assistente ha accesso in tempo reale allo stato della casa per fornire risposte precise.

### 🌐 Connettività & Stabilità
*   **Dual-Connection**: Riconoscimento automatico della rete (**WIFI** locale o **RETE** cellulare) con switch intelligente degli indirizzi IP.
*   **Stabilità MQTT**: Implementazione di riconnessione automatica e monitoraggio persistente dello stato del broker con indicatore visivo nell'header.
*   **Interfaccia Moderna**: Design in Dark Mode con transizioni fluide, navigazione a 5 tab e selettore di modalità (Dashboard vs AI) immersivo.

---

## 🛠️ Tech Stack

*   **Linguaggio**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Architettura**: MVVM (Model-View-ViewModel)
*   **AI Engine**: Google Generative AI SDK (Gemini 3)
*   **Networking**: Eclipse Paho MQTT Client
*   **Database**: Room Persistence Library (per il logging locale)
*   **Data Parsing**: Moshi (JSON adapter)

---

## ⚙️ Configurazione

Per collegare l'app al proprio sistema domotico:
1.  Aprire il menu di configurazione tramite l'icona **Tune (ingranaggio)** nell'header.
2.  Inserire i parametri del Broker MQTT (IP Locale, IP Remoto, Porta, Credenziali).
3.  Impostare il **Base Topic** (es. `zara/casa`) che l'app userà come radice per tutte le sottoscrizioni e pubblicazioni.

---

*Sviluppato con ❤️ per una casa più intelligente e reattiva.*
