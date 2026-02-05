package com.example.blockex


    import android.view.View
    import android.widget.ImageView
    import android.widget.TextView
    import com.kizitonwose.calendar.view.ViewContainer

// O simplemente busca los IDs manualmente:
    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView = view.findViewById<TextView>(R.id.calendarDayText)
        val heartIcon = view.findViewById<ImageView>(R.id.exFiveDaySelectionView)
    }
