package com.example.blockex

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class SeleccionTiempoActivity : AppCompatActivity() {

    private lateinit var meses: TextInputEditText
    private lateinit var semanas: TextInputEditText
    private lateinit var dias: TextInputEditText

    private var lastAngle = 0f
    private var rotationAccumulator = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seleccion_tiempo)

        // Ajuste de padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de vistas
        meses = findViewById(R.id.textoMeses)
        semanas = findViewById(R.id.textoSemanas)
        dias = findViewById(R.id.textoDias)

        val rueda = findViewById<ImageView>(R.id.rueda)

        // Listener para girar la rueda
        rueda.setOnTouchListener { v, event ->
            val centerX = v.width / 2f
            val centerY = v.height / 2f
            val x = event.x - centerX
            val y = event.y - centerY
            val angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble())).toFloat()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastAngle = angle // inicializamos al tocar
                }
                MotionEvent.ACTION_MOVE -> {
                    var delta = angle - lastAngle

                    // Normalizar delta para evitar saltos al cruzar -180/180
                    if (delta > 180) delta -= 360
                    if (delta < -180) delta += 360

                    // Girar la rueda suavemente
                    v.rotation += delta * 0.5f

                    // Acumular delta para actualizar tiempo
                    rotationAccumulator += delta

                    // Incrementar/decrementar tiempo cuando se supera umbral s
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
    }

    private fun incrementarTiempo() {
        val d = (dias.text.toString().toIntOrNull() ?: 0) + 1
        val s = (semanas.text.toString().toIntOrNull() ?: 0)
        val m = (meses.text.toString().toIntOrNull() ?: 0)
        var newD = d
        var newS = s
        var newM = m

        if (newD > 6) {
            newD = 0
            newS++
        }
        if (newS > 3) {
            newS = 0
            newM++
        }

        dias.setText(newD.toString().padStart(2, '0'))
        semanas.setText(newS.toString().padStart(2, '0'))
        meses.setText(newM.toString().padStart(2, '0'))
    }

    private fun decrementarTiempo() {
        val d = (dias.text.toString().toIntOrNull() ?: 0) - 1
        val s = (semanas.text.toString().toIntOrNull() ?: 0)
        val m = (meses.text.toString().toIntOrNull() ?: 0)
        var newD = d
        var newS = s
        var newM = m

        if (newD < 0) {
            newD = 6
            newS--
        }
        if (newS < 0) {
            newS = 3
            newM--
        }
        if (newM < 0) newM = 0

        dias.setText(newD.toString().padStart(2, '0'))
        semanas.setText(newS.toString().padStart(2, '0'))
        meses.setText(newM.toString().padStart(2, '0'))
    }
}
