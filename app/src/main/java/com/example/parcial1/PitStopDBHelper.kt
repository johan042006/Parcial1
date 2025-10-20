package com.example.parcial1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PitStopDBHelper(context: Context) : SQLiteOpenHelper(context, "pitstopDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE pitstop (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                driverName TEXT,
                team TEXT,
                stopTime REAL,
                tireType TEXT,
                tireCount INTEGER,
                status TEXT,
                failureReason TEXT,
                mechanic TEXT,
                dateTime TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pitstop")
        onCreate(db)
    }

    fun insertPitStop(p: PitStop) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("driverName", p.driverName)
            put("team", p.team)
            put("stopTime", p.stopTime)
            put("tireType", p.tireType)
            put("tireCount", p.tireCount)
            put("status", p.status)
            put("failureReason", p.failureReason)
            put("mechanic", p.mechanic)
            put("dateTime", p.dateTime)
        }
        db.insert("pitstop", null, values)
        db.close()
    }

    fun getAllPitStops(): List<PitStop> {
        val list = mutableListOf<PitStop>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pitstop ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    PitStop(
                        id = cursor.getInt(0),
                        driverName = cursor.getString(1),
                        team = cursor.getString(2),
                        stopTime = cursor.getDouble(3),
                        tireType = cursor.getString(4),
                        tireCount = cursor.getInt(5),
                        status = cursor.getString(6),
                        failureReason = cursor.getString(7),
                        mechanic = cursor.getString(8),
                        dateTime = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun deletePitStop(id: Int) {
        val db = writableDatabase
        db.delete("pitstop", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun searchPitStops(query: String): List<PitStop> {
        val list = mutableListOf<PitStop>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM pitstop WHERE driverName LIKE ? OR team LIKE ?",
            arrayOf("%$query%", "%$query%")
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    PitStop(
                        id = cursor.getInt(0),
                        driverName = cursor.getString(1),
                        team = cursor.getString(2),
                        stopTime = cursor.getDouble(3),
                        tireType = cursor.getString(4),
                        tireCount = cursor.getInt(5),
                        status = cursor.getString(6),
                        failureReason = cursor.getString(7),
                        mechanic = cursor.getString(8),
                        dateTime = cursor.getString(9)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getAverageTime(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT AVG(stopTime) FROM pitstop", null)
        val avg = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        db.close()
        return avg
    }

    fun updatePitStop(p: PitStop) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("driverName", p.driverName)
            put("team", p.team)
            put("stopTime", p.stopTime)
            put("tireType", p.tireType)
            put("tireCount", p.tireCount)
            put("status", p.status)
            put("failureReason", p.failureReason)
            put("mechanic", p.mechanic)
            put("dateTime", p.dateTime)
        }
        db.update("pitstop", values, "id = ?", arrayOf(p.id.toString()))
        db.close()
    }
}