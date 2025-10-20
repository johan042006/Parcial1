package com.example.pitstop

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
}