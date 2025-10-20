package com.example.parcial1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }
}