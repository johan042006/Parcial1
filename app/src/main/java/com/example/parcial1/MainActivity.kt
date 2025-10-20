package com.example.parcial1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
                "summary" -> PitStopSummaryView(db) { currentView = it }
                "edit" -> PitStopCreateView(db, onBack = { currentView = "summary" })
                "list" -> PitStopListView(
                    db,
                    onBack = { currentView = "summary" },
                    onEdit = { pit ->
                        pitToEdit = pit
                        currentView = "editExisting"
                    }
                )
                "editExisting" -> pitToEdit?.let { pit ->
                    PitStopEditView(pit, db) {
                        pitToEdit = null
                        currentView = "list"
                    }
                }
            }
        }
    }
}

@Composable
fun PitStopListView(
    db: PitStopDBHelper,
    onBack: () -> Unit,
    onEdit: (PitStop) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var pitStops by remember { mutableStateOf(db.getAllPitStops()) }
    Spacer(Modifier.height(12.dp))
    Button(onClick = { onBack() }, modifier = Modifier.fillMaxWidth()) {
        Text("⬅ Volver al resumen")
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("📋 Lista de Pit Stops", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = search,
            onValueChange = {
                search = it
                pitStops = if (search.isEmpty()) db.getAllPitStops() else db.searchPitStops(search)
            },
            label = { Text("Buscar por piloto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(pitStops) { pit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E))
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text("Piloto: ${pit.driverName}", color = Color.White)
                        Text("Escudería: ${pit.team}", color = Color.LightGray)
                        Text("Tiempo: ${pit.stopTime}s", color = Color(0xFFBB86FC))
                        Text("Neumáticos: ${pit.tireType} (${pit.tireCount})", color = Color.LightGray)
                        Text("Estado: ${pit.status}", color = if (pit.status == "Ok") Color.Green else Color.Red)
                        Text("Mecánico: ${pit.mechanic}", color = Color.White)
                        Text("Fecha/Hora: ${pit.dateTime}", color = Color(0xFFBB86FC))
                        if (pit.failureReason.isNotEmpty()) Text("Fallo: ${pit.failureReason}", color = Color.Red)

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { onEdit(pit) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Editar")
                            }

                            Button(
                                onClick = {
                                    db.deletePitStop(pit.id)
                                    pitStops = db.getAllPitStops()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PitStopEditView(pit: PitStop, db: PitStopDBHelper, onBack: () -> Unit) {
    var driver by remember { mutableStateOf(pit.driverName) }
    var team by remember { mutableStateOf(pit.team) }
    var time by remember { mutableStateOf(pit.stopTime.toString()) }
    var tireType by remember { mutableStateOf(pit.tireType) }
    var tireCount by remember { mutableStateOf(pit.tireCount.toString()) }
    var status by remember { mutableStateOf(pit.status) }
    var failureReason by remember { mutableStateOf(pit.failureReason) }
    var mechanic by remember { mutableStateOf(pit.mechanic) }
    var dateTime by remember { mutableStateOf(pit.dateTime) }

    val tireOptions = listOf("Soft", "Medium", "Hard")
    val statusOptions = listOf("Ok", "Fallo")
    var tireMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("✏ Editar Pit Stop", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

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

        OutlinedTextField(value = failureReason, onValueChange = { failureReason = it }, label = { Text("Motivo del fallo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = mechanic, onValueChange = { mechanic = it }, label = { Text("Mecánico principal") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha y hora del Pit Stop") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    val updatedPit = pit.copy(
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
                    db.updatePitStop(updatedPit)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Actualizar")
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

@Composable
fun PitStopSummaryView(db: PitStopDBHelper, navigate: (String) -> Unit) {
    val pitStops = db.getAllPitStops()
    val avgTime = db.getAverageTime()

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("📊 Resumen de Pit Stops", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Text("Total de pilotos: ${pitStops.size}", color = Color.LightGray)
        Text("Promedio de Tiempo: ${"%.2f".format(avgTime)}s", color = Color(0xFFBB86FC))

        if (pitStops.isNotEmpty()) {
            val fastest = pitStops.minByOrNull { it.stopTime }
            Spacer(Modifier.height(8.dp))
            Text("Pit stop más rápido: ${fastest?.driverName} (${fastest?.stopTime}s)", color = Color.Green)
            Spacer(Modifier.height(24.dp))
            Text("Tiempos por piloto:", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            PitStopBarChart(pitStops)
        } else {
            Spacer(Modifier.height(16.dp))
            Text("No hay registros aún.", color = Color.Gray)
        }

        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navigate("edit") }) { Text("➕ Registrar piloto") }
            Button(onClick = { navigate("list") }) { Text("📋 Ver listado") }
        }
    }
}

@Composable
fun PitStopBarChart(pitStops: List<PitStop>) {
    val maxTime = pitStops.maxOfOrNull { it.stopTime } ?: 1.0
    val barHeight = 24.dp

    Column {
        pitStops.forEach { pit ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(pit.driverName, color = Color.White, modifier = Modifier.width(90.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .padding(start = 8.dp, end = 24.dp)
                ) {
                    val widthFactor = (pit.stopTime / maxTime).toFloat()
                    drawRoundRect(
                        color = Color(0xFFBB86FC),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width * widthFactor, size.height),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
                Text("${pit.stopTime}s", color = Color.LightGray, fontSize = 12.sp)
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