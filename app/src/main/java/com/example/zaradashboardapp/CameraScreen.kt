package com.example.zaradashboardapp

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.zaradashboardapp.ui.theme.DarkBackground
import com.example.zaradashboardapp.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val tcSettings = remember { settingsManager.getTinyCamSettings() }
    val scope = rememberCoroutineScope()
    
    // Pager per gestire le 6 telecamere
    val pagerState = rememberPagerState(pageCount = { 6 })

    // Rilevamento rete per scegliere l'IP corretto (Locale o Remoto)
    val currentIp = remember(tcSettings) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        
        if (isWifi) tcSettings.ip else (if (tcSettings.remoteIp.isNotEmpty()) tcSettings.remoteIp else tcSettings.ip)
    }

    // Riferimento alla WebView corrente per il refresh
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header con titolo e indicatore camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TINYCAM MONITOR PRO",
                    style = MaterialTheme.typography.titleMedium,
                    color = TealPrimary
                )
                Text(
                    text = "Telecamera ${pagerState.currentPage + 1} di 6",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            IconButton(onClick = { webViewInstance?.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Ricarica", tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (currentIp.isBlank()) {
                Text("Configura l'IP di tinyCam nel menu Setup", color = Color.Gray)
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                    beyondViewportPageCount = 0,
                    key = { it }
                ) { page ->
                    val cameraIndex = page + 1
                    val cameraUrl = "http://$currentIp:${tcSettings.port}/axis-cgi/mjpg/video.cgi?camera=$cameraIndex&fps=1&compression=80&resolution=320x240&t=${System.currentTimeMillis()}"
                    
                    // Overlay di caricamento locale per ogni pagina
                    var isLoading by remember { mutableStateOf(true) }
                    LaunchedEffect(page) {
                        isLoading = true
                        kotlinx.coroutines.delay(2000)
                        isLoading = false
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    webViewClient = object : WebViewClient() {
                                        override fun onReceivedHttpAuthRequest(
                                            view: WebView?,
                                            handler: HttpAuthHandler?,
                                            host: String?,
                                            realm: String?
                                        ) {
                                            if (tcSettings.user.isNotEmpty() && tcSettings.pass.isNotEmpty()) {
                                                handler?.proceed(tcSettings.user, tcSettings.pass)
                                            } else {
                                                super.onReceivedHttpAuthRequest(view, handler, host, realm)
                                            }
                                        }
                                    }
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    setBackgroundColor(android.graphics.Color.BLACK)

                                    loadUrl(cameraUrl)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.33f),
                            update = { view ->
                                if (page == pagerState.currentPage) {
                                    webViewInstance = view
                                }
                            },
                            onRelease = { view ->
                                view.stopLoading()
                                view.loadUrl("about:blank")
                                view.clearHistory()
                                view.removeAllViews()
                                view.destroy()
                            }
                        )

                        if (isLoading) {
                            CircularProgressIndicator(
                                color = TealPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                // Frecce Overlay (visibili solo se necessario)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        enabled = pagerState.currentPage > 0
                    ) {
                        if (pagerState.currentPage > 0) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        }
                    }

                    IconButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        enabled = pagerState.currentPage < 5
                    ) {
                        if (pagerState.currentPage < 5) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
