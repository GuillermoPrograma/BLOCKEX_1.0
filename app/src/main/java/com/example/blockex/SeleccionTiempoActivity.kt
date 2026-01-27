package com.example.blockex

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import androidx.preference.PreferenceManager
import java.time.LocalDate

class SeleccionTiempoActivity : AppCompatActivity() {

    private lateinit var meses: TextInputEditText
    private lateinit var semanas: TextInputEditText
    private lateinit var dias: TextInputEditText
    private lateinit var botonAceptar: Button

    private var lastAngle = 0f
    private var rotationAccumulator = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Saltar la pantalla si ya está configurada, iría el calendario de mar
        /*  if (estaConfigurado()) {
              startActivity(Intent(this, MainActivity::class.java))
              finish()
              return
          }*/

        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccion_tiempo)

        // Ajuste de padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        meses = findViewById(R.id.textoMeses)
        semanas = findViewById(R.id.textoSemanas)
        dias = findViewById(R.id.textoDias)
        botonAceptar = findViewById(R.id.selecciontiempoConfirms)

        cargarValoresPorDefecto() // opcional: 0 o 1

        val rueda = findViewById<ImageView>(R.id.rueda)

        // Listener para girar la rueda
        rueda.setOnTouchListener { v, event ->
            val centerX = v.width / 2f
            val centerY = v.height / 2f
            val x = event.x - centerX
            val y = event.y - centerY
            val angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> lastAngle = angle
                MotionEvent.ACTION_MOVE -> {
                    var delta = angle - lastAngle
                    if (delta > 180) delta -= 360
                    if (delta < -180) delta += 360
                    v.rotation += delta * 0.5f
                    rotationAccumulator += delta

                    if (rotationAccumulator > 15f) {
                        incrementarTiempo()
                        rotationAccumulator = 0f
                    } else if (rotationAccumulator < -15f) {
                        decrementarTiempo()
                        rotationAccumulator = 0f
                    }
                    lastAngle = angle
                }
            }
            true
        }

        // Botón Confirmar
        botonAceptar.setOnClickListener {
            guardarTiempoEnPreferencias()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Incrementar y decrementar
    private fun incrementarTiempo() {
        var d = (dias.text.toString().toIntOrNull() ?: 0) + 1
        var s = (semanas.text.toString().toIntOrNull() ?: 0)
        var m = (meses.text.toString().toIntOrNull() ?: 0)

        if (d > 6) { d = 0; s++ }
        if (s > 3) { s = 0; m++ }

        dias.setText(d.toString().padStart(2,'0'))
        semanas.setText(s.toString().padStart(2,'0'))
        meses.setText(m.toString().padStart(2,'0'))
    }

    private fun decrementarTiempo() {
        var d = (dias.text.toString().toIntOrNull() ?: 0) - 1
        var s = (semanas.text.toString().toIntOrNull() ?: 0)
        var m = (meses.text.toString().toIntOrNull() ?: 0)

        if (d < 0) { d = 6; s-- }
        if (s < 0) { s = 3; m-- }
        if (m < 0) m = 0

        dias.setText(d.toString().padStart(2,'0'))
        semanas.setText(s.toString().padStart(2,'0'))
        meses.setText(m.toString().padStart(2,'0'))
    }

    // Guardar en SharedPreferences
    private fun guardarTiempoEnPreferencias() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val fechaHoy = LocalDate.now().toString() // Formato "YYYY-MM-DD"
        prefs.edit().apply {
            putInt("tiempo_meses", meses.text.toString().toIntOrNull() ?: 0)
            putInt("tiempo_semanas", semanas.text.toString().toIntOrNull() ?: 0)
            putInt("tiempo_dias", dias.text.toString().toIntOrNull() ?: 0)

            putString("fecha_inicio_configuracion", fechaHoy)


            putBoolean("tiempo_configurado", true) // marca que ya configuró


            apply()
        }
    }

    // Revisar si ya configuró
/*  private fun estaConfigurado(): Boolean {
      val prefs = PreferenceManager.getDefaultSharedPreferences(this)
      return prefs.getBoolean("tiempo_configurado", false)
  }*/

  // Opcional: valores iniciales por defecto
  private fun cargarValoresPorDefecto() {
      meses.setText("01")
      semanas.setText("00")
      dias.setText("00")
  }
}
