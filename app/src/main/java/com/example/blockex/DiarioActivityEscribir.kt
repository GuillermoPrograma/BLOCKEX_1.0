package com.example.blockex

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.blockex.bbdd.DiarioDao
import com.example.blockex.bbdd.DiarioDatabase
import com.example.blockex.bbdd.DiarioEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DiarioActivityEscribir : AppCompatActivity() {

    private lateinit var editTextDiario: EditText
    private lateinit var tvFechaHoy: TextView
    private lateinit var btnGuardar: ImageButton
    private lateinit var dao: DiarioDao
    private lateinit var fechaSeleccionada: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diario_escribir)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        editTextDiario = findViewById(R.id.editTextDiario)
        tvFechaHoy = findViewById(R.id.tvFechaHoy)
        btnGuardar = findViewById(R.id.btnGuardar)

        //Recibir fecha desde el calendario
        fechaSeleccionada = intent.getStringExtra("FECHA")
            ?.let { LocalDate.parse(it) }
            ?: LocalDate.now()

        //Cambiar la fecha para que no se quede siempre la de hoy si venimos del calendario
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        tvFechaHoy.text = fechaSeleccionada.format(formato)

        dao = DiarioDatabase.getDatabase(this).diarioDao()

        //CArgar el texto del dia
        lifecycleScope.launch {
            val entrada = dao.obtenerPorFecha(fechaSeleccionada.toString())
            if (entrada != null) {
                editTextDiario.setText(entrada.texto)
            }
        }

        btnGuardar.setOnClickListener {
            val texto = editTextDiario.text.toString()

            lifecycleScope.launch {
                dao.guardar(
                    DiarioEntry(
                        fecha = fechaSeleccionada.toString(),
                        texto = texto
                    )
                )
                finish()
            }
        }

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.setupNavigation(this, R.id.nav_diario)
    }
}