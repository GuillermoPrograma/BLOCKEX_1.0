package com.example.blockex.bbdd

import androidx.room.*

@Dao
interface DiarioDao {

    @Query("SELECT * FROM diario WHERE fecha = :fecha")
    suspend fun obtenerPorFecha(fecha: String): DiarioEntry?

    //EL REPLACE SI EXISTE ACTUALIZA Y SINO INSERTA
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(entry: DiarioEntry)

    @Query("SELECT fecha FROM diario")
    suspend fun obtenerFechasConEntrada(): List<String>
}