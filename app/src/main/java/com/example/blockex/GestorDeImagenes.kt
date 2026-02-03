package com.example.blockex

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.os.*
import android.provider.MediaStore
import java.io.File

object GestorDeImagenes {

    fun borrarDefinitivo(context: Context) {
        File(context.filesDir, "hidden_images").deleteRecursively()
    }

    fun restaurarFotos(context: Context) {
        val hiddenDir = File(context.filesDir, "hidden_images")

        hiddenDir.listFiles()?.forEach { file ->
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return@forEach

            context.contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().copyTo(output)
            }
        }

        borrarDefinitivo(context)
    }
}