package com.example.blockex

import androidx.annotation.RequiresApi

import android.app.AlertDialog
import android.content.ContentValues
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import java.io.File

class SelectorFotos : AppCompatActivity() {

    // Lista de imágenes seleccionadas por el usuario
    private var fotosSeleccionadas: List<Uri> = emptyList()

    /**
     * Selector de múltiples imágenes usando
     * Storage Access Framework (muy estable)
     */
    private val seleccionarImgs =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            fotosSeleccionadas = uris
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selector_fotos)

        val btnSelect = findViewById<Button>(R.id.btnSelect)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        // Mostrar consentimiento al iniciar la app
        consentimiento()

        // Botón para seleccionar imágenes
        btnSelect.setOnClickListener {
            seleccionarImgs.launch(arrayOf("image/*"))
        }

        // Botón para confirmar acción
        btnConfirm.setOnClickListener {
            if (fotosSeleccionadas.isNotEmpty()) {
                ocultarYBorrar(fotosSeleccionadas)
            } else {
                showMessage("No has seleccionado imágenes")
            }
        }
    }

    /**
     * Diálogo de consentimiento obligatorio
     */
    private fun consentimiento() {
        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage(
                "Esta app ocultará temporalmente las imágenes seleccionadas " +
                        "durante 1 minuto. Después podrás borrarlas definitivamente " +
                        "o restaurarlas a la galería.\n\n¿Aceptas continuar?"
            )
            .setCancelable(false)
            .setPositiveButton("Acepto", null)
            .setNegativeButton("Cancelar") { _, _ -> finish() }
            .show()
    }

    /**
     * Copia imágenes a almacenamiento interno (oculto)
     * y solicita al sistema borrar las originales
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun ocultarYBorrar(uris: List<Uri>) {
        val directorioOculto = File(cacheDir, "hidden_images")
        directorioOculto.mkdirs()

        // Copiar cada imagen
        uris.forEach { uri ->
            copiarAlDirectorio(uri, directorioOculto)
        }

        // Solicitar borrado de la galería (diálogo del sistema)
        borradoTemporal(uris)

        // Esperar 1 minuto
        Handler(Looper.getMainLooper()).postDelayed({
            borrarORecuperar()
        }, 60_000)
    }

    /**
     * Copia el archivo completo (con metadatos)
     */
    private fun copiarAlDirectorio(uri: Uri, dir: File) {
        val input = contentResolver.openInputStream(uri) ?: return
        val file = File(dir, "${System.currentTimeMillis()}.jpg")

        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    /**
     * Pide al sistema borrar las imágenes seleccionadas
     */
    private fun borradoTemporal(uris: List<Uri>) {
        uris.forEach { uri ->
            try {
                DocumentsContract.deleteDocument(contentResolver, uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Diálogo después de 1 minuto
     */
    private fun borrarORecuperar() {
        AlertDialog.Builder(this)
            .setTitle("Tiempo finalizado")
            .setMessage("¿Qué deseas hacer con las imágenes?")
            .setPositiveButton("Borrar definitivamente") { _, _ ->
                borrarDefinitivo()
                showMessage("Imágenes borradas definitivamente")
            }
            .setNegativeButton("Restaurar a la galería") { _, _ ->
                restaurarFotos()
                showMessage("Imágenes restauradas")
            }
            .show()
    }

    /**
     * Restaura las imágenes a la galería
     */
    private fun restaurarFotos() {
        val hiddenDir = File(cacheDir, "hidden_images")

        hiddenDir.listFiles()?.forEach { file ->
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                )
            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return@forEach

            contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().copyTo(output)
            }
        }

        borrarDefinitivo()
    }

    /**
     * Borra definitivamente los archivos ocultos
     */
    private fun borrarDefinitivo() {
        File(cacheDir, "hidden_images").deleteRecursively()
    }

    /**
     * Mensajes simples al usuario
     */
    private fun showMessage(text: String) {
        AlertDialog.Builder(this)
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }
}
