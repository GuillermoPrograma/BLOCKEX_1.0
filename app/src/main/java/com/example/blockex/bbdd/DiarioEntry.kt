package com.example.blockex.bbdd

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diario")
data class DiarioEntry(
    @PrimaryKey val fecha: String,
    val texto: String
)