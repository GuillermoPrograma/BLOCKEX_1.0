package com.example.blockex

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Función para que todos tus compañeros la usen en sus Activity
 */
fun BottomNavigationView.setupNavigation(activity: Activity, selectedItemId: Int) {
    this.selectedItemId = selectedItemId

    this.setOnItemSelectedListener { item ->
        if (item.itemId == selectedItemId) return@setOnItemSelectedListener true

        when (item.itemId) {
            R.id.nav_diario -> {
                activity.startActivity(Intent(activity, DiarioActivity::class.java))
                activity.overridePendingTransition(0, 0)
                true
            }
            R.id.nav_calendario -> {
                activity.startActivity(Intent(activity, CalendarioActivity::class.java))
                activity.overridePendingTransition(0, 0)
                true
            }
            R.id.nav_ajustes -> {
                activity.startActivity(Intent(activity, AjustesActivity::class.java))
                activity.overridePendingTransition(0, 0)
                true
            }
            else -> false
        }
    }
}