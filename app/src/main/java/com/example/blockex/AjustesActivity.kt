package com.example.blockex

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AjustesActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ajustes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val botonPrivacidad=findViewById<Button>(R.id.button2)
        botonPrivacidad.setOnClickListener {
            mostrarDialogoAvisoLegal()

        }
    }
    private fun mostrarDialogoAvisoLegal() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Aviso Legal")
        builder.setMessage("Para usar esta tienda debes aceptar los términos y condiciones de uso.")
        builder.setPositiveButton("Aceptar", null)
        builder.setNegativeButton("Salir") { _, _ ->
            finish() // Cierra la Activity actual (la app)
        }
        builder.setCancelable(false) // Impedimos que se cierre tocando fuera del mensaje
        builder.show()
    }
}