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
    val id: Int = 0, // id autoincremental (por defecto 0 hasta insertarse)
    val driverName: String, // nombre del piloto
    val team: String, // escudería/equipo
    val stopTime: Double, // tiempo del pit stop en segundos (double)
    val tireType: String, // tipo de neumático (Soft/Medium/Hard)
    val tireCount: Int, // número de neumáticos cambiados
    val status: String, // estado (Ok / Fallo)
    val failureReason: String, // motivo del fallo si aplica
    val mechanic: String, // nombre del mecánico principal
    val dateTime: String // fecha y hora del pit stop como String
)
// Data class que representa un registro de Pit Stop en la app

@OptIn(ExperimentalMaterial3Api::class)
// Opt-in para usar APIs experimentales de Material3 en Compose
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Llama al lifecycle de la Activity
        setContent {
            // Punto de entrada de Compose: define la UI
            MaterialTheme(colorScheme = darkColorScheme()) {
                // Aplica tema oscuro global
                PitStopApp(this)
                // Llama al composable principal pasando el contexto de la Activity
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
// Opt-in para Material3 dentro del composable
@Composable
fun PitStopApp(context: android.content.Context) {
    val db = PitStopDBHelper(context)
    // Crea una instancia del helper de BD con el contexto de la Activity

    var currentView by remember { mutableStateOf("summary") }
    // Estado que controla qué pantalla se muestra ("summary", "edit", "list", etc.)

    var pitToEdit by remember { mutableStateOf<PitStop?>(null) }
    // Estado para almacenar el PitStop seleccionado para editar (nullable)

    Scaffold(
        // Scaffold proporciona TopAppBar, floating action button, etc.
        topBar = {
            TopAppBar(
                title = { Text("🏁 Pit Stops F1") },
                // Título del top app bar con emoji
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    // Color del contenedor del top bar (oscuro)
                    titleContentColor = Color.White
                    // Color del texto del título
                )
            )
        },
        containerColor = Color(0xFF1E1E1E)
        // Color del fondo del scaffold
    ) { padding ->
        // Content lambda recibe padding para respetar barras del sistema
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo el espacio disponible
                .padding(padding) // Respeta el padding del scaffold
                .background(Color(0xFF1E1E1E)), // Fondo oscuro
            horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente
        ) {
            // Navegación simple basada en string
            when (currentView) {
                "summary" -> PitStopSummaryView(db) { currentView = it }
                // Muestra la vista resumen; callback para cambiar pantalla

                "edit" -> PitStopCreateView(db, onBack = { currentView = "summary" })
                // Muestra la vista de crear nuevo pit stop

                "list" -> PitStopListView(
                    db,
                    onBack = { currentView = "summary" },
                    onEdit = { pit ->
                        pitToEdit = pit // Guarda el pit seleccionado para editar
                        currentView = "editExisting" // Cambia a vista de edición
                    }
                )
                // Muestra la lista de PitStops con callbacks

                "editExisting" -> pitToEdit?.let { pit ->
                    PitStopEditView(pit, db) {
                        pitToEdit = null // Limpia el pit seleccionado
                        currentView = "list" // Vuelve al listado
                    }
                }
                // Si hay un pit seleccionado, muestra la vista de edición
            }
        }
    }
}

@Composable
fun PitStopListView(
    db: PitStopDBHelper, // helper de BD para obtener/editar/eliminar registros
    onBack: () -> Unit, // callback para volver a la vista anterior
    onEdit: (PitStop) -> Unit // callback para editar un PitStop
) {
    var search by remember { mutableStateOf("") }
    // Estado para el texto de búsqueda

    var pitStops by remember { mutableStateOf(db.getAllPitStops()) }
    // Estado con la lista de pit stops, inicializa desde la BD

    Spacer(Modifier.height(12.dp))
    // Espacio superior

    Button(onClick = { onBack() }, modifier = Modifier.fillMaxWidth()) {
        Text("⬅ Volver al resumen")
    }
    // Botón para volver al resumen (colocado arriba)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Contenedor principal con padding
        Text("📋 Lista de Pit Stops", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        // Título de la pantalla

        Spacer(modifier = Modifier.height(8.dp))
        // Separador

        OutlinedTextField(
            value = search,
            onValueChange = {
                search = it
                pitStops = if (search.isEmpty()) db.getAllPitStops() else db.searchPitStops(search)
                // Cuando cambia la búsqueda, actualiza la lista desde la BD o filtra
            },
            label = { Text("Buscar por piloto") },
            modifier = Modifier.fillMaxWidth()
        )
        // Campo de búsqueda por nombre de piloto o equipo

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            // Lista perezosa para mostrar muchos elementos eficientemente
            items(pitStops) { pit ->
                // Itera cada PitStop y crea una Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E))
                ) {
                    Column(Modifier.padding(10.dp)) {
                        // Información básica del pit stop
                        Text("Piloto: ${pit.driverName}", color = Color.White)
                        Text("Escudería: ${pit.team}", color = Color.LightGray)
                        Text("Tiempo: ${pit.stopTime}s", color = Color(0xFFBB86FC))
                        Text("Neumáticos: ${pit.tireType} (${pit.tireCount})", color = Color.LightGray)
                        Text("Estado: ${pit.status}", color = if (pit.status == "Ok") Color.Green else Color.Red)
                        Text("Mecánico: ${pit.mechanic}", color = Color.White)
                        Text("Fecha/Hora: ${pit.dateTime}", color = Color(0xFFBB86FC))
                        if (pit.failureReason.isNotEmpty()) Text("Fallo: ${pit.failureReason}", color = Color.Red)
                        // Si hay motivo de fallo, lo muestra en rojo

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón editar -> llama al callback onEdit con el pit seleccionado
                            Button(
                                onClick = { onEdit(pit) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Editar")
                            }

                            // Botón eliminar -> elimina de la BD y refresca la lista
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
    // Estados locales inicializados con los valores del PitStop recibido
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
    // Opciones para el dropdown de tipo de neumático

    val statusOptions = listOf("Ok", "Fallo")
    // Opciones para estado

    var tireMenuExpanded by remember { mutableStateOf(false) }
    // Estado para abrir/cerrar menú de neumáticos

    var statusMenuExpanded by remember { mutableStateOf(false) }
    // Estado para abrir/cerrar menú de estado

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("✏ Editar Pit Stop", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        // Título de la vista de edición

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Piloto") }, modifier = Modifier.fillMaxWidth())
        // Campo para editar el nombre del piloto

        OutlinedTextField(value = team, onValueChange = { team = it }, label = { Text("Escudería") }, modifier = Modifier.fillMaxWidth())
        // Campo para editar el equipo

        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Tiempo total (s)") }, modifier = Modifier.fillMaxWidth())
        // Campo para editar el tiempo; el valor se guardará como String y al actualizar se parsea a Double

        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            // Contenedor para el campo de selección de neumáticos y su dropdown
            OutlinedTextField(
                value = tireType,
                onValueChange = {},
                label = { Text("Cambio de neumáticos") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            // Campo readOnly que solo muestra la opción seleccionada

            DropdownMenu(expanded = tireMenuExpanded, onDismissRequest = { tireMenuExpanded = false }) {
                tireOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        tireType = option
                        tireMenuExpanded = false
                    })
                    // Cada opción actualiza tireType y cierra el menú
                }
            }
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { tireMenuExpanded = true }
            )
            // Overlay transparente para abrir el menú al hacer click en el campo
        }

        OutlinedTextField(value = tireCount, onValueChange = { tireCount = it }, label = { Text("Número de neumáticos") }, modifier = Modifier.fillMaxWidth())
        // Campo para número de neumáticos (string que luego se parsea a Int)

        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            // Contenedor para campo de estado y su dropdown
            OutlinedTextField(
                value = status,
                onValueChange = {},
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            // Campo readOnly para mostrar estado

            DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                statusOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        status = option
                        statusMenuExpanded = false
                    })
                    // Selección de estado
                }
            }
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .clickable { statusMenuExpanded = true }
            )
            // Overlay para abrir menú de estado
        }

        OutlinedTextField(value = failureReason, onValueChange = { failureReason = it }, label = { Text("Motivo del fallo") }, modifier = Modifier.fillMaxWidth())
        // Campo para describir motivo del fallo (si aplica)

        OutlinedTextField(value = mechanic, onValueChange = { mechanic = it }, label = { Text("Mecánico principal") }, modifier = Modifier.fillMaxWidth())
        // Campo para el nombre del mecánico

        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha y hora del Pit Stop") }, modifier = Modifier.fillMaxWidth())
        // Campo para la fecha/hora (aquí se usa String; podrías integrar DatePicker)

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // Fila con botones Actualizar y Cancelar
            Button(
                onClick = {
                    // Al presionar Actualizar, crea una copia del pit con los nuevos valores
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
                    db.updatePitStop(updatedPit) // Actualiza en la BD
                    onBack() // Llama al callback para volver a la pantalla anterior
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Actualizar")
            }

            Button(
                onClick = { onBack() }, // Cancela y vuelve sin guardar cambios
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
    // Obtiene todos los pit stops desde la BD

    val avgTime = db.getAverageTime()
    // Calcula el promedio de stopTime desde la BD

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("📊 Resumen de Pit Stops", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        // Título del resumen

        Spacer(Modifier.height(16.dp))

        Text("Total de pilotos: ${pitStops.size}", color = Color.LightGray)
        // Muestra el número total de registros

        Text("Promedio de Tiempo: ${"%.2f".format(avgTime)}s", color = Color(0xFFBB86FC))
        // Muestra el promedio formateado con 2 decimales

        if (pitStops.isNotEmpty()) {
            val fastest = pitStops.minByOrNull { it.stopTime }
            // Encuentra el pit stop más rápido por stopTime

            Spacer(Modifier.height(8.dp))
            Text("Pit stop más rápido: ${fastest?.driverName} (${fastest?.stopTime}s)", color = Color.Green)
            // Muestra piloto más rápido

            Spacer(Modifier.height(24.dp))
            Text("Tiempos por piloto:", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            PitStopBarChart(pitStops) // Llama al gráfico de barras
        } else {
            Spacer(Modifier.height(16.dp))
            Text("No hay registros aún.", color = Color.Gray)
            // Mensaje si no hay registros
        }

        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botones para navegar a crear o ver listado
            Button(onClick = { navigate("edit") }) { Text("➕ Registrar piloto") }
            Button(onClick = { navigate("list") }) { Text("📋 Ver listado") }
        }
    }
}

@Composable
fun PitStopBarChart(pitStops: List<PitStop>) {
    val maxTime = pitStops.maxOfOrNull { it.stopTime } ?: 1.0
    // Calcula el tiempo máximo para escalar las barras (evita división por 0)

    val barHeight = 24.dp
    // Altura de las barras

    Column {
        pitStops.forEach { pit ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Text(pit.driverName, color = Color.White, modifier = Modifier.width(90.dp))
                // Muestra nombre del piloto con ancho fijo

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .padding(start = 8.dp, end = 24.dp)
                ) {
                    val widthFactor = (pit.stopTime / maxTime).toFloat()
                    // Factor entre 0 y 1 proporcional al tiempo del pit respecto al máximo

                    drawRoundRect(
                        color = Color(0xFFBB86FC),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width * widthFactor, size.height),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    // Dibuja la barra con tamaño proporcional
                }
                Text("${pit.stopTime}s", color = Color.LightGray, fontSize = 12.sp)
                // Muestra el valor numérico al final de la fila
            }
        }
    }
}

@Composable
fun PitStopCreateView(db: PitStopDBHelper, onBack: () -> Unit) {
    // Estados para cada campo del formulario
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
    // Opciones de neumáticos para el dropdown

    val statusOptions = listOf("Ok", "Fallo")
    // Opciones de estado para el dropdown

    var tireMenuExpanded by remember { mutableStateOf(false) }
    // Estado para abrir/cerrar menú de neumáticos

    var statusMenuExpanded by remember { mutableStateOf(false) }
    // Estado para abrir/cerrar menú de estado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("➕ Registrar Pit Stop", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        // Título del formulario

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = driver, onValueChange = { driver = it }, label = { Text("Piloto") }, modifier = Modifier.fillMaxWidth())
        // Campo para nombre del piloto

        OutlinedTextField(value = team, onValueChange = { team = it }, label = { Text("Escudería") }, modifier = Modifier.fillMaxWidth())
        // Campo para el equipo

        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Tiempo total (s)") }, modifier = Modifier.fillMaxWidth())
        // Campo para tiempo; aquí se acepta String y al guardar se parsea a Double

        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = tireType,
                onValueChange = {},
                label = { Text("Cambio de neumáticos") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            // Campo readOnly para mostrar tipo de neumático seleccionado

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
            // Overlay para abrir el menú al tocar el campo
        }

        OutlinedTextField(value = tireCount, onValueChange = { tireCount = it }, label = { Text("Número de neumáticos") }, modifier = Modifier.fillMaxWidth())
        // Campo para número de neumáticos (se parsea a Int al guardar)

        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = status,
                onValueChange = {},
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            // Campo readOnly para estado

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
            // Overlay para abrir el menú de estado
        }

        OutlinedTextField(value = failureReason, onValueChange = { failureReason = it }, label = { Text("Motivo del fallo (si aplica)") }, modifier = Modifier.fillMaxWidth())
        // Campo opcional para motivo de fallo

        OutlinedTextField(value = mechanic, onValueChange = { mechanic = it }, label = { Text("Mecánico principal") }, modifier = Modifier.fillMaxWidth())
        // Campo para nombre del mecánico

        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Fecha y hora del Pit Stop") }, modifier = Modifier.fillMaxWidth())
        // Campo para fecha/hora (string). Podrías automatizar o usar selector de fecha/hora

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = {
                    // Al presionar Guardar, crea el objeto PitStop a partir de los estados
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
                    db.insertPitStop(pit) // Inserta en la BD
                    onBack() // Regresa a la vista anterior
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text("Guardar")
            }

            Button(
                onClick = { onBack() }, // Cancela la creación y vuelve
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Cancelar")
            }
        }
    }
}