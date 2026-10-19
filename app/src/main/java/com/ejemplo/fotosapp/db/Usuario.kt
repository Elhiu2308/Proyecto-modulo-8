package com.ejemplo.fotosapp.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario registrado en la base de datos local (SQLite a través de Room).
 * La contraseña se guarda cifrada con SHA-256 (no en texto plano).
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val contrasena: String
)
