package com.example.blockex

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

    private val CHANNEL_ID = "canal_ajustes_blockex"
    private val NOTIFICATION_ID = 101
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ajustes)

        // Ajuste de márgenes para pantallas modernas
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val checkBoxNotificaciones = findViewById<CheckBox>(R.id.checkBox)
        val botonPrivacidad = findViewById<Button>(R.id.button2)

        // 1. Crear el canal (Obligatorio para que funcionen las notificaciones)
        createNotificationChannel()

        // 2. Lógica del CheckBox
        checkBoxNotificaciones.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si el usuario marca la casilla...
                if (checkPermission()) {
                    // Si ya tenemos permiso, lanzamos la notificación directa
                    mostrarNotificacionPersonalizada()
                } else {
                    // Si NO tenemos permiso, lo pedimos
                    // IMPORTANTE: Aquí llamamos a la función de abajo, no escribimos el código aquí dentro
                    checkBoxNotificaciones.isChecked = false // Desmarcamos hasta que acepte
                    requestPermission()
                }
            } else {
                Toast.makeText(this, "Notificaciones desactivadas", Toast.LENGTH_SHORT).show()
            }
        }

        botonPrivacidad.setOnClickListener {
            mostrarDialogoAvisoLegal()
        }
        val tvAyuda = findViewById<TextView>(R.id.textView2)

// Configura el clic
        tvAyuda.setOnClickListener {
            val url = "https://fundacionpsf.org/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.setupNavigation(this, R.id.nav_ajustes)
    }

    // -----------------------------------------------------------------------
    // AQUÍ ES DONDE VA EL CÓDIGO (FUNCIONES FUERA DEL ONCREATE)
    // -----------------------------------------------------------------------

    private fun requestPermission() {
        // Solo pedimos permiso en Android 13 (TIRAMISU) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // ESTE ES EL BLOQUE DE CÓDIGO POR EL QUE PREGUNTABAS:
            ActivityCompat.requestPermissions(
                this, // Al estar en una función de la clase, 'this' ES la Activity. ¡Correcto!
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    // Este método recibe la respuesta del usuario (SI o NO al permiso)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario dijo SÍ: Marcamos el check y lanzamos la notificación
                val checkBox = findViewById<CheckBox>(R.id.checkBox)
                checkBox.isChecked = true
                mostrarNotificacionPersonalizada()
            } else {
                // El usuario dijo NO
                Toast.makeText(this, "Permiso denegado. No podrás recibir avisos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarNotificacionPersonalizada() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Ajustes Actualizados")
            .setContentText("Has activado las notificaciones de BlockEx.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(this@AjustesActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ajustes Generales"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = "Notificaciones de configuración"
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun mostrarDialogoAvisoLegal() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Aviso Legal")
        builder.setMessage(
                "\n" +
                "En BlockEx, entendemos que la información que usted almacena en nuestra plataforma —desde sus reflexiones personales en el diario hasta sus fotografías y registros de calendario— constituye una parte fundamental de su intimidad. Por ello, la protección de su privacidad no es solo una obligación legal para nosotros, sino el pilar central de nuestra ética operativa.\n" +
                "\n" +
                "1. Naturaleza de la Información Reconocemos que BlockEx alberga datos de carácter altamente sensible. Tratamos todas las entradas de texto, archivos multimedia (fotografías/vídeos) y patrones de uso con la máxima confidencialidad.\n" +
                "\n" +
                "2. Almacenamiento y Soberanía de Datos Su información le pertenece exclusivamente a usted. BlockEx está diseñada para priorizar el almacenamiento seguro. No tenemos acceso al contenido de sus entradas ni a sus fotografías personales. No comercializamos, cedemos ni compartimos su información con terceros para fines publicitarios ni de análisis de comportamiento.\n" +
                "\n" +
                "3. Seguridad de Archivos Multimedia Entendemos el valor sentimental y privado de las imágenes adjuntas a sus registros. Implementamos protocolos de seguridad para garantizar que estas solo sean accesibles a través de la aplicación y bajo su autorización explícita.\n" +
                "\n" +
                "4. Control del Usuario Usted mantiene el control total sobre sus datos. BlockEx garantiza su derecho a acceder, modificar, exportar o eliminar permanentemente cualquier información registrada en la aplicación en el momento que lo desee.\n" +
                "\n" +
                "Agradecemos la confianza depositada en BlockEx para salvaguardar sus recuerdos y pensamientos más personales.\n" +
                "\n"
               )
        builder.setPositiveButton("Aceptar", null)
        builder.show()
    }
}