package com.example.blockex

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
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
import java.time.temporal.ChronoUnit
import java.util.Locale

class CalendarioActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var contadorTxt: TextView
    private lateinit var legendLayout: LinearLayout
    private lateinit var monthText: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private val selectedDates = mutableSetOf<LocalDate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Activar el modo de pantalla completa (EdgeToEdge)
        enableEdgeToEdge()

        setContentView(R.layout.activity_calendario)

        // 2. REGLA DE ORO: Evitar que el menú se suba
        // Buscamos el ID "main" que debe tener tu ConstraintLayout/RelativeLayout principal
        val mainView = findViewById<View>(R.id.calendarView)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplicamos padding arriba para no tapar la hora/batería
            // Pero ponemos 0 ABAJO para que el menú esté pegado al borde
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // 3. INICIALIZACIÓN DE VISTAS
        calendarView = findViewById(R.id.calendarView)
        contadorTxt = findViewById(R.id.contadorTxt)
        legendLayout = findViewById(R.id.legendLayout)
        monthText = findViewById(R.id.monthText)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)

        // 4. LLAMADA A FUNCIONES DE CONFIGURACIÓN
        configurarCalendario()
        configurarCabeceraDias()
        configurarNavegacionMeses()
        actualizarContadorSanacion()
        verificarPlazo()

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.setupNavigation(this, R.id.nav_calendario)
    }

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
        calendarView.monthScrollListener = { month ->
            val mesNombre = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { it.uppercase() }
            monthText.text = "$mesNombre ${month.yearMonth.year}"
        }

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

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val selectionView: ImageView = view.findViewById(R.id.exFiveDaySelectionView)
    }

    private fun verificarPlazo() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fechaInicioStr = prefs.getString("fecha_inicio_configuracion", null)
        val tiempoConfigurado = prefs.getBoolean("tiempo_configurado", false)

        if (fechaInicioStr != null && tiempoConfigurado) {
            val fechaInicio = LocalDate.parse(fechaInicioStr)
            val m = prefs.getInt("tiempo_meses", 0).toLong()
            val s = prefs.getInt("tiempo_semanas", 0).toLong()
            val d = prefs.getInt("tiempo_dias", 0).toLong()

            val fechaVencimiento = fechaInicio.plusMonths(m).plusWeeks(s).plusDays(d)
            val hoy = LocalDate.now()
            val diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVencimiento)

            if (diasRestantes <= 0) {
                ejecutarAccionFinDePlazo()
            }
        }
    }

    private fun ejecutarAccionFinDePlazo() {
        // AQUI ES DONDE vA LO DE PILAR
    }

    private fun actualizarContadorSanacion() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val fechaInicioStr = prefs.getString("fecha_inicio_configuracion", null)

        if (fechaInicioStr != null) {
            val fechaInicio = LocalDate.parse(fechaInicioStr)
            val hoy = LocalDate.now()
            val diasTranscurridos = maxOf(0, ChronoUnit.DAYS.between(fechaInicio, hoy))
            contadorTxt.text = "Llevas $diasTranscurridos usando Blockex"
        } else {
            contadorTxt.text = "Configura tu tiempo para empezar"
        }
    }
}