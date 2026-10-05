package com.alfonsokitoko.gestionpedidos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alfonsokitoko.gestionpedidos.data.model.Pedido

// Define base de datos Room
@Database(entities = [Pedido::class], version = 3)
// Necesario para convertir las fechas
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	// Acceso al DAO de Pedido
	abstract fun pedidoDao(): PedidoDao

	companion object {
		// Visible en todos los hilos
		@Volatile
		private var INSTANCE: AppDatabase? = null

		fun getDatabase(context: Context): AppDatabase {
			// Si ya existe, devuelve la instancia, si no se crea
			return INSTANCE ?: synchronized(this) {
				val instance = Room.databaseBuilder(
					context.applicationContext,
					AppDatabase::class.java,
					"pedidos_db"
				)
					.fallbackToDestructiveMigration(true)
					.build()
				// Guarda la instancia creada
				INSTANCE = instance
				instance
			}
		}
	}
}