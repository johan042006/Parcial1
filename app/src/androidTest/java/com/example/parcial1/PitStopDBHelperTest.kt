package com.example.parcial1

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PitStopDBHelperTest {

    private lateinit var dbHelper: PitStopDBHelper
    private lateinit var context: Context

    @Before
    fun setUp() {
        // Crea un contexto de prueba
        context = ApplicationProvider.getApplicationContext()
        dbHelper = PitStopDBHelper(context)

        // Limpia la tabla antes de cada test
        val db = dbHelper.writableDatabase
        db.delete("pitstop", null, null)
        db.close()
    }

    @After
    fun tearDown() {
        // Cierra la base de datos después de cada prueba
        dbHelper.close()
    }

    @Test
    fun testInsertAndRetrievePitStop() {
        val pitStop = PitStop(
            id = 0,
            driverName = "Hamilton",
            team = "Mercedes",
            stopTime = 2.4,
            tireType = "Soft",
            tireCount = 4,
            status = "Ok",
            failureReason = "",
            mechanic = "Luis Pérez",
            dateTime = "2025-10-17 10:00"
        )

        dbHelper.insertPitStop(pitStop)

        val allPits = dbHelper.getAllPitStops()

        assertTrue(allPits.isNotEmpty()) // Verifica que haya registros
        assertEquals("Hamilton", allPits[0].driverName)
        assertEquals("Mercedes", allPits[0].team)
        assertEquals(2.4, allPits[0].stopTime, 0.01)
    }

    @Test
    fun testDeletePitStop() {
        val pitStop = PitStop(
            id = 0,
            driverName = "Verstappen",
            team = "Red Bull",
            stopTime = 2.1,
            tireType = "Medium",
            tireCount = 4,
            status = "Ok",
            failureReason = "",
            mechanic = "Carlos Rojas",
            dateTime = "2025-10-17 11:00"
        )

        dbHelper.insertPitStop(pitStop)

        val beforeDelete = dbHelper.getAllPitStops()
        assertTrue(beforeDelete.isNotEmpty())

        val idToDelete = beforeDelete[0].id
        dbHelper.deletePitStop(idToDelete)

        val afterDelete = dbHelper.getAllPitStops()
        assertTrue(afterDelete.isEmpty())
    }
}