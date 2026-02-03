package com.example.blockex

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var fechaTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        val boton=findViewById<Button>(R.id.button3)
        boton.setOnClickListener{
            val intent= Intent(this, AjustesActivity::class.java)
            startActivity(intent)
        }

        val botonTiempo=findViewById<Button>(R.id.seleccionTiempo)
        botonTiempo.setOnClickListener{
            val intent= Intent(this, SeleccionTiempoActivity::class.java)
            startActivity(intent)
        }

        val botonDiario=findViewById<Button>(R.id.DiarioBtn)
        botonDiario.setOnClickListener{

            val  intent= Intent(this, DiarioActivityEscribir ::class.java)
            startActivity(intent)
        }
        val calendarBtn=findViewById<Button>(R.id.btnCal)
        calendarBtn.setOnClickListener{

            val  intent= Intent(this, CalendarioActivity ::class.java)
            startActivity(intent)
        }

        val botonFotos = findViewById<Button>(R.id.seleccionImg)
        calendarBtn.setOnClickListener{

            val  intent= Intent(this, SelectorFotos ::class.java)
            startActivity(intent)
        }

        fechaTextView = findViewById(R.id.fecha)
        mostrarFechaConfigurada()

    }
    private fun mostrarFechaConfigurada() {
        // Leer los valores guardados
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fechaString = prefs.getString("fecha_inicio_configuracion", null)
        val meses = prefs.getInt("tiempo_meses", 0)
        val semanas = prefs.getInt("tiempo_semanas", 0)
        val dias = prefs.getInt("tiempo_dias", 0)
        if (fechaString != null) {
            val fechaGuardada = LocalDate.parse(fechaString)
            // Ejemplo: ¿Qué día será en 1 mes?
            println("La configuración se hizo el: $fechaGuardada")
        }
        val texto = "${meses} meses, ${semanas} semanas, ${dias} días"
        fechaTextView.text = texto
    }
}
