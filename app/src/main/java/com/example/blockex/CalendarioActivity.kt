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
                        contadorTxt.text = "Llevas ${selectedDates.size} días de sanación"
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
}