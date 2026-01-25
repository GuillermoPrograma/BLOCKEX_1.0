package com.example.blockex
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class ConfigFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
} //Aqui  le digo donde guardar las preferencias