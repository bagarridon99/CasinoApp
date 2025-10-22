package com.example.casinoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.casinoapp.data.dao.UserDao
import com.example.casinoapp.data.entity.UserEntity

/**
 * Clase principal de la base de datos local usando Room.
 * Aquí se definen las entidades y los DAOs que formarán parte de la BD.
 * - version = 3: versión del esquema (se usa para migraciones)
 * - exportSchema = false: evita generar archivos de esquema JSON.
 */
@Database(entities = [UserEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // DAO asociado a la entidad UserEntity, para operaciones CRUD.
    abstract fun userDao(): UserDao

    companion object {
        // Singleton para asegurar que solo exista una instancia de la BD.
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Devuelve la instancia única de la base de datos.
         * Si no existe, se crea con Room.databaseBuilder().
         * fallbackToDestructiveMigration(): destruye y recrea la BD en cambios de versión.
         */
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "casino_db" // Nombre físico del archivo de BD en el dispositivo.
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
