package com.example.parcial1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                "edit" -> PitStopCreateView(db, onBack = { currentView = "summary" })
                "list" -> Text("Vista Listado (pendiente)", color = Color.White)
                "editExisting" -> pitToEdit?.let { pit -> Text("Vista Editar (pendiente)", color = Color.White) }
            }
        }
    }
}

@Composable
fun PitStopCreateView(db: PitStopDBHelper, onBack: () -> Unit) {
    var driver by remember { mutableStateOf("") }
    var team by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var tireType by remember { mutableStateOf("Soft") }
    var tireCount by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ok") }
    var failureReason by remember { mutableStateOf("") }
    var mechanic by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }

    val tireOptions = listOf("Soft", "Medium", "Hard")
    val statusOptions = listOf("Ok", "Fallo")
    var tireMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("➕ Registrar Pit Stop", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Piloto") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = team, onValueChange = { team = it }, label = { Text("Escudería") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Tiempo total (s)") }, modifier = Modifier.fillMaxWidth())

        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = tireType,
                onValueChange = {},
                label = { Text("Cambio de neumáticos") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            DropdownMenu(expanded = tireMenuExpanded, onDismissRequest = { tireMenuExpanded = false }) {
                tireOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        tireType = option
                        tireMenuExpanded = false
                    })
                }
            }
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { tireMenuExpanded = true }
            )
        }

        OutlinedTextField(value = tireCount, onValueChange = { tireCount = it }, label = { Text("Número de neumáticos") }, modifier = Modifier.fillMaxWidth())

        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = status,
                onValueChange = {},
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                statusOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        status = option
                        statusMenuExpanded = false
                    })
                }
            }
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { statusMenuExpanded = true }
            )
        }

        OutlinedTextField(value = failureReason, onValueChange = { failureReason = it }, label = { Text("Motivo del fallo (si aplica)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = mechanic, onValueChange = { mechanic = it }, label = { Text("Mecánico principal") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha y hora del Pit Stop") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    val pit = PitStop(
                        driverName = driver,
                        team = team,
                        stopTime = time.toDoubleOrNull() ?: 0.0,
                        tireType = tireType,
                        tireCount = tireCount.toIntOrNull() ?: 0,
                        status = status,
                        failureReason = failureReason,
                        mechanic = mechanic,
                        dateTime = dateTime
                    )
                    db.insertPitStop(pit)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Guardar")
            }

            Button(
                onClick = { onBack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Cancelar")
            }
        }
    }
}