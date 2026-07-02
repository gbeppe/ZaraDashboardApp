package com.example.zaradashboardapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.LogDao

class DashboardViewModelFactory(
    private val application: Application,
    private val logDao: LogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application, logDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
