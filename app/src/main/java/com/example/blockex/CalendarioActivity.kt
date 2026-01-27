package com.example.blockex

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.time.temporal.ChronoUnit
import androidx.preference.PreferenceManager
import android.util.Log
class CalendarioActivity : AppCompatActivity() {

    // 1. PROPIEDADES DE LA CLASE (Fuera de onCreate)
    private lateinit var calendarView: CalendarView
    private lateinit var contadorTxt: TextView
    private lateinit var legendLayout: LinearLayout
    private lateinit var monthText: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private val selectedDates = mutableSetOf<LocalDate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        // 2. INICIALIZACIÓN DE VISTAS (Dentro de onCreate)
        calendarView = findViewById(R.id.calendarView)
        contadorTxt = findViewById(R.id.contadorTxt)
        legendLayout = findViewById(R.id.legendLayout)
        monthText = findViewById(R.id.monthText)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)

        // 3. LLAMADA A FUNCIONES DE CONFIGURACIÓNS
        configurarCalendario()
        configurarCabeceraDias()
        configurarNavegacionMeses()

        actualizarContadorSanacion()
        // ... dentro de onCreate ...
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.setupNavigation(this, R.id.nav_calendario)
    }

    // 4. MÉTODOS DE CONFIGURACIÓN (Fuera de onCreate para que sea limpio)

    private fun configurarCalendario() {
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    actualizarVisualizacionDia(container, data.date)

                    container.view.setOnClickListener {
                        val date = data.date
                        if (selectedDates.contains(date)) selectedDates.remove(date)
                        else selectedDates.add(date)

                        calendarView.notifyDateChanged(date)
                      //  contadorTxt.text = "Llevas ${selectedDates.size} días de sanación"
                    }
                } else {
                    container.textView.visibility = View.INVISIBLE
                    container.selectionView.visibility = View.GONE
                }
            }
        }

        val currentMonth = YearMonth.now()
        calendarView.setup(currentMonth.minusMonths(12), currentMonth.plusMonths(12), DayOfWeek.MONDAY)
        calendarView.scrollToMonth(currentMonth)
    }

    private fun configurarCabeceraDias() {
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        legendLayout.removeAllViews()
        daysOfWeek.forEach { dayOfWeek ->
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                // Cambiamos Miércoles por X
                text = if (dayOfWeek == DayOfWeek.WEDNESDAY) "X"
                else dayOfWeek.getDisplayName(TextStyle.NARROW, Locale("es")).uppercase()
                setTextColor(Color.parseColor("#4E4E4E"))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }
            legendLayout.addView(textView)
        }
    }

    private fun configurarNavegacionMeses() {
        // Actualizar el título cuando se desliza el calendario
        calendarView.monthScrollListener = { month ->
            val mesNombre = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { it.uppercase() }
            monthText.text = "$mesNombre ${month.yearMonth.year}"
        }

        // Flechas
        btnPrevMonth.setOnClickListener {
            val visibleMonth = calendarView.findFirstVisibleMonth()?.yearMonth ?: return@setOnClickListener
            calendarView.smoothScrollToMonth(visibleMonth.minusMonths(1))
        }

        btnNextMonth.setOnClickListener {
            val visibleMonth = calendarView.findFirstVisibleMonth()?.yearMonth ?: return@setOnClickListener
            calendarView.smoothScrollToMonth(visibleMonth.plusMonths(1))
        }
    }

    private fun actualizarVisualizacionDia(container: DayViewContainer, date: LocalDate) {
        if (selectedDates.contains(date)) {
            container.selectionView.visibility = View.VISIBLE
            container.textView.setTextColor(Color.WHITE)
        } else {
            container.selectionView.visibility = View.GONE
            container.textView.setTextColor(Color.BLACK)
        }
    }

    // 5. EL VIEWCONTAINER (Siempre fuera de las funciones, dentro de la clase)
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        // Cambiamos View por ImageView
        val selectionView: ImageView = view.findViewById(R.id.exFiveDaySelectionView)
    }



    private fun verificarPlazo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // 1. Recuperar la fecha en que se configuró
        val fechaInicioStr = prefs.getString("fecha_inicio_configuracion", null)
        val tiempoConfigurado = prefs.getBoolean("tiempo_configurado", false)

        if (fechaInicioStr != null && tiempoConfigurado) {
            // Convertir el String guardado de vuelta a LocalDate
            val fechaInicio = LocalDate.parse(fechaInicioStr)

            // 2. Recuperar el tiempo que el usuario eligió
            val m = prefs.getInt("tiempo_meses", 0).toLong()
            val s = prefs.getInt("tiempo_semanas", 0).toLong()
            val d = prefs.getInt("tiempo_dias", 0).toLong()

            // 3. Calcular la fecha de vencimiento sumando los periodos
            val fechaVencimiento = fechaInicio
                .plusMonths(m)
                .plusWeeks(s)
                .plusDays(d)

            val hoy = LocalDate.now()

            // 4. Calcular días restantes usando ChronoUnit que sirve para calcular fechas de manera exacta
            val diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento)

            // 5. Lógica de control
            if (diasRestantes <= 0) {

                ejecutarAccionFinDePlazo()
            } else {

                //Aqui podriamos meter algo si es importante, aqui es donde le decimos que todavia le quedan
            }
        }
    }

    private fun ejecutarAccionFinDePlazo() {
        //aqui donde va la parte de pilar
    }

    private fun actualizarContadorSanacion() { //esto para actualizar los días que llevas en la app!!
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fechaInicioStr = prefs.getString("fecha_inicio_configuracion", null)

        if (fechaInicioStr != null) {
            val fechaInicio = LocalDate.parse(fechaInicioStr)
            val hoy = LocalDate.now()

            // Calculamos los días que han pasado desde el inicio hasta hoy
            // Usamos maxOf(0, ...) para evitar números negativos si el usuario configuró la fecha para el futuro
            val diasTranscurridos = maxOf(0, ChronoUnit.DAYS.between(fechaInicio, hoy))

            contadorTxt.text = "Llevas $diasTranscurridos usando Blockex"
        } else {
            contadorTxt.text = "Configura tu tiempo para empezar"
        }
    }

}