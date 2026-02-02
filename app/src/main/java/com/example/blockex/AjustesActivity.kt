package com.example.blockex

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class AjustesActivity : AppCompatActivity() {

    private val CHANNEL_ID_POPUP = "canal_popup_blockex_urgente"
    private val NOTIFICATION_ID = 777
    private val CODIGO_PETICION_PERMISOS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ajustes)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Referencias UI
        val checkBoxNotificaciones = findViewById<CheckBox>(R.id.checkBox)
        val botonPrivacidad = findViewById<Button>(R.id.button2)
        val botonPermisos = findViewById<Button>(R.id.button)
        val tvAyuda = findViewById<TextView>(R.id.textView2)
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Inicializar
        createHighPriorityChannel()
        nav.setupNavigation(this, R.id.nav_ajustes)

        // Estado inicial del CheckBox (si tiene permisos, sale marcado)
        checkBoxNotificaciones.isChecked = checkPermission()

        // --- 1. BOTÓN PERMISOS (CAMBIO SOLICITADO) ---
        botonPermisos.setOnClickListener {
            // Simplemente abrimos la pantalla de ajustes de la app
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        // --- 2. LÓGICA DEL CHECKBOX (NATIVA) ---
        checkBoxNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkPermission()) {
                    lanzarNotificacionPopUp()
                } else {
                    // Desmarcamos visualmente y pedimos permiso nativo
                    checkBoxNotificaciones.isChecked = false
                    pedirPermisoNativoAndroid()
                }
            } else {
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 3. OTROS BOTONES ---
        botonPrivacidad.setOnClickListener { mostrarDialogoAvisoLegal() }

        tvAyuda.setOnClickListener {
            val url = "https://fundacionpsf.org/"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    // --- FUNCIONES DE PERMISOS NATIVOS ---

    private fun pedirPermisoNativoAndroid() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                CODIGO_PETICION_PERMISOS
            )
        } else {
            // Android antiguos
            val checkBox = findViewById<CheckBox>(R.id.checkBox)
            checkBox.isChecked = true
            lanzarNotificacionPopUp()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CODIGO_PETICION_PERMISOS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Usuario dijo ALLOW
                val checkBox = findViewById<CheckBox>(R.id.checkBox)
                checkBox.isChecked = true
                lanzarNotificacionPopUp()
            } else {
                // Usuario dijo DON'T ALLOW
                Toast.makeText(this, "Es necesario aceptar el permiso para recibir avisos.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- FUNCIONES DE NOTIFICACIÓN ---

    private fun lanzarNotificacionPopUp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_POPUP)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡BlockEx Activado!")
            .setContentText("Permisos concedidos. !¡Puedes recibir notificaciones!!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createHighPriorityChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Avisos Urgentes BlockEx"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID_POPUP, name, importance).apply {
                description = "Notificaciones Pop-up"
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun mostrarDialogoAvisoLegal() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Aviso Legal y Privacidad")
        builder.setMessage(
            "1. Información General: El uso de esta aplicación implica la aceptación de estos términos.\n\n" +
                    "2. Propiedad Intelectual: Todos los contenidos, diseños y códigos son propiedad exclusiva del desarrollador o sus licenciantes.\n\n" +
                    "3. Responsabilidad: El desarrollador no se hace responsable del uso indebido de la app ni de posibles fallos técnicos temporales.\n\n" +
                    "4. Privacidad: Los datos facilitados por el usuario se tratarán conforme al RGPD, garantizando su confidencialidad y seguridad. No se cederán datos a terceros sin consentimiento previo.\n\n" +
                    "5. Derechos: Puede ejercer sus derechos de acceso, rectificación y supresión a través del correo de soporte proporcionado en la ficha de la aplicación."
        )
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}