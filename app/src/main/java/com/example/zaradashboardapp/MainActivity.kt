package com.example.zaradashboardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.zaradashboardapp.ui.theme.ZaraDashboardAppTheme
import androidx.lifecycle.ViewModelProvider
import com.example.database.DatabaseProvider
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = DatabaseProvider.getDatabase(applicationContext)
        val logDao = db.logDao()
        val factory = DashboardViewModelFactory(application, logDao)
        val dashboardViewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        setContent {
            ZaraDashboardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val aiViewModel: AiAssistantViewModel = viewModel()

                    MainScreen(
                        dashboardViewModel = dashboardViewModel,
                        aiViewModel = aiViewModel
                    )
                }
            }
        }
    }
}
