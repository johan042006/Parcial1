package com.example.pitstop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.parcial1.PitStopDBHelper

data class PitStop(
    val id: Int = 0,
    val driverName: String,
    val team: String,
    val stopTime: Double,
    val tireType: String,
    val tireCount: Int,
    val status: String,
    val failureReason: String,
    val mechanic: String,
    val dateTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PitStopApp(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitStopApp(context: android.content.Context) {
    val db = PitStopDBHelper(context)
    var currentView by remember { mutableStateOf("summary") }
    var pitToEdit by remember { mutableStateOf<PitStop?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏁 Pit Stops F1") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1E1E1E)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentView) {
                "summary" -> Text("Vista Resumen (pendiente)", color = Color.White)
                "edit" -> Text("Vista Crear (pendiente)", color = Color.White)
                "list" -> Text("Vista Listado (pendiente)", color = Color.White)
                "editExisting" -> pitToEdit?.let { pit -> Text("Vista Editar (pendiente)", color = Color.White) }
            }
        }
    }
}