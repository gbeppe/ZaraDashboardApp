package com.example.zaradashboardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.zaradashboardapp.ui.theme.ZaraDashboardAppTheme
import androidx.lifecycle.ViewModelProvider
import com.example.database.DatabaseProvider
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Ottieni il DAO dal tuo DatabaseProvider
        val db = DatabaseProvider.getDatabase(applicationContext)
        val logDao = db.logDao()

        // 2. Crea la Factory passando l'applicazione e il DAO
        val factory = DashboardViewModelFactory(application, logDao)

        // 3. Istanzia il ViewModel usando la Factory
        val viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
/*
        setContent {
            ZaraDashboardAppTheme {
                MainScreen(viewModel)
            }
        }
 */
        setContent {
            ZaraDashboardAppTheme { // O il nome del tema della tua app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ---------------------------------------------------------
                    // 1. COMMENTA LA TUA NAVIGAZIONE NORMALE:
                    // MainNavigationGraph(...)
                    // oppure
                    // DashboardScreen(...)
                    // ---------------------------------------------------------

                    // ---------------------------------------------------------
                    // 2. INSERISCI IL TEST DELLA GENERATIVE UI:
                    // Inizializza il ViewModel legato al ciclo di vita dell'Activity
                    val aiViewModel: AiAssistantViewModel = viewModel()

                    // Lancia la schermata passandole i ViewModel
                    GenerativeUiScreen(
                        aiViewModel = aiViewModel,
                        dashboardViewModel = viewModel
                    )
                    // ---------------------------------------------------------
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ZaraDashboardAppTheme {
        Greeting("Android")
    }
}
