package com.example.blockex

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiarioActivityEscribir : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diario_escribir)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val tvFechaHoy = findViewById<TextView>(R.id.tvFechaHoy)
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaHoy = formato.format(Date())

        tvFechaHoy.text = fechaHoy
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.setupNavigation(this, R.id.nav_diario)

    }
}