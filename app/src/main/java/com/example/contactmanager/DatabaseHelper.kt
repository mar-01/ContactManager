package com.example.contactmanager

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS Contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Contacts")
        onCreate(db)
    }

    fun addContact(name: String, phone: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
        }
        val newRowId = db.insert("Contacts", null, values) // Füge den Kontakt zur Datenbank hinzu
        if (newRowId == -1L) {
            Log.e("DatabaseHelper", "Error inserting contact")
        } else {
            Log.d("DatabaseHelper", "Contact added with ID: $newRowId")
        }
        db.close() // Datenbank schließen
    }

    fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor = db.query("Contacts", null, null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val name = getString(getColumnIndexOrThrow("name"))
                val phone = getString(getColumnIndexOrThrow("phone"))
                contacts.add(Contact(id, name, phone)) // Füge das Contact-Objekt zur Liste hinzu
            }
        }
        cursor.close()
        db.close()
        return contacts
    }

    fun getContactIdByName(name: String): Long? {
        val db = readableDatabase
        val cursor = db.query("Contacts", arrayOf("id"), "name = ?", arrayOf(name), null, null, null)

        return if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            cursor.close()
            id
        } else {
            cursor.close()
            null // Wenn kein Kontakt gefunden wurde
        }
    }

    fun deleteContact(id: Long) {
        val db = writableDatabase
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete("Contacts", selection, selectionArgs)
        db.close()
    }
}