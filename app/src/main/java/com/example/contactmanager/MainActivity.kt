package com.example.contactmanager

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar



class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var buttonAdd: Button
    private lateinit var listViewContacts: ListView
    private lateinit var contactAdapter: ArrayAdapter<String>
    private var contactList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        editTextName = findViewById(R.id.editTextName)
        editTextPhone = findViewById(R.id.editTextPhone)
        buttonAdd = findViewById(R.id.buttonAdd)
        listViewContacts = findViewById(R.id.listViewContacts)

        contactAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactList)
        listViewContacts.adapter = contactAdapter

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        loadContacts()

        buttonAdd.setOnClickListener {
            addContact()
        }

        listViewContacts.setOnItemClickListener { _, _, position, _ ->
            val selectedContact = contactList[position]

            // Teile den String in Name und Telefonnummer
            val parts = selectedContact.split(" - ")
            if (parts.size == 2) {
                val contactName = parts[0]

                // ID des Kontakts abrufen
                val contactId = dbHelper.getContactIdByName(contactName)

                // Zeige Bestätigungsdialogfeld an
                if (contactId != null) {
                    showDeleteConfirmationDialog(contactId.toLong(), contactName)
                }
            } else {
                // Fehlerbehandlung
                Log.e("MainActivity", "Selected contact format is incorrect: $selectedContact")
            }
        }
    }

    private fun loadContacts() {
        contactList.clear()
        val contacts = dbHelper.getAllContacts()
        for (contact in contacts) {
            contactList.add("${contact.name} - ${contact.phone}")
        }
        contactAdapter.notifyDataSetChanged()
    }

    private fun addContact() {
        val name = editTextName.text.toString()
        val phone = editTextPhone.text.toString()
        if (name.isNotEmpty() && phone.isNotEmpty()) {
            dbHelper.addContact(name, phone)
            editTextName.text.clear()
            editTextPhone.text.clear()
            loadContacts() // Aktualisiere die Liste
        }
    }

    private fun showDeleteConfirmationDialog(contactId: Long, contactName: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete $contactName?")
            .setPositiveButton("Yes") { _, _ ->
                dbHelper.deleteContact(contactId) // Löschen
                loadContacts() // Aktualisieren
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}