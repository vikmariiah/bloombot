package project.team36.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import project.team36.data.local.dao.SavedLocationDao
import project.team36.data.local.dao.SavedPlantDao
import project.team36.model.location.SavedLocation
import project.team36.model.location.SavedPlant

@Database(
    entities = [
        SavedLocation::class,
        SavedPlant::class
    ],
    version = 6,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun savedPlantDao(): SavedPlantDao

    // Singleton object to create a database for the app
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    //Deletes and recreates the database if there is a version mismatch
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}