package com.ritesh.cashiro.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ritesh.cashiro.data.local.dao.AccountDao
import com.ritesh.cashiro.data.local.dao.ActivityLogDao
import com.ritesh.cashiro.data.local.dao.CategoryDao
import com.ritesh.cashiro.data.local.dao.SubCategoryDao
import com.ritesh.cashiro.data.local.dao.TransactionDao
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.ActivityLogEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        SubCategoryEntity::class,
        ActivityLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TransactionDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun subCategoryDao(): SubCategoryDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        const val DATABASE_NAME = "transaction_database"

        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getDatabase(context: Context): TransactionDatabase {
            Log.d("Database", "Initializing database")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    DATABASE_NAME
                )
                    //                    .addCallback(object : Callback() {
                    //                        override fun onCreate(db: SupportSQLiteDatabase) {
                    //                            super.onCreate(db)
                    //                        }
                    //                    })
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
//@Database(
//    entities = [
//        TransactionEntity::class,
//        CategoryEntity::class,
//        AccountEntity::class,
//        SubCategoryEntity::class,
//        ActivityLogEntity::class // NEW: Added ActivityLogEntity
//    ],
//    version = 2, // Updated version from 1 to 2
//    exportSchema = false
//)
//@TypeConverters(Converters::class)
//abstract class TransactionDatabase : RoomDatabase() {
//
//    abstract fun transactionDao(): TransactionDao
//    abstract fun categoryDao(): CategoryDao
//    abstract fun accountDao(): AccountDao
//    abstract fun subCategoryDao(): SubCategoryDao
//    abstract fun activityLogDao(): ActivityLogDao // NEW: Added ActivityLogDao
//
//    companion object {
//        const val DATABASE_NAME = "transaction_database"
//
//        @Volatile
//        private var INSTANCE: TransactionDatabase? = null
//
//        // Migration from version 1 to 2 (adding activity_logs table)
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // Create the activity_logs table
//                database.execSQL("""
//                    CREATE TABLE IF NOT EXISTS `activity_logs` (
//                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                        `actionType` TEXT NOT NULL,
//                        `title` TEXT NOT NULL,
//                        `description` TEXT NOT NULL,
//                        `timestamp` INTEGER NOT NULL,
//                        `relatedTransactionId` INTEGER,
//                        `relatedAccountId` INTEGER,
//                        `relatedCategoryId` INTEGER,
//                        `amount` REAL,
//                        `oldValue` TEXT,
//                        `newValue` TEXT,
//                        `metadata` TEXT NOT NULL,
//                        FOREIGN KEY(`relatedTransactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
//                        FOREIGN KEY(`relatedAccountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
//                        FOREIGN KEY(`relatedCategoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
//                    )
//                """.trimIndent())
//
//                // Create indices for performance
//                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_timestamp` ON `activity_logs` (`timestamp`)")
//                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_actionType` ON `activity_logs` (`actionType`)")
//                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_relatedAccountId` ON `activity_logs` (`relatedAccountId`)")
//                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_relatedTransactionId` ON `activity_logs` (`relatedTransactionId`)")
//                database.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_relatedCategoryId` ON `activity_logs` (`relatedCategoryId`)")
//            }
//        }
//
//        fun getDatabase(context: Context): TransactionDatabase {
//            Log.d("Database", "Initializing database")
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    TransactionDatabase::class.java,
//                    DATABASE_NAME
//                )
//                    .addMigrations(MIGRATION_1_2) // Add the migration
//                    .addCallback(object : Callback() {
//                        override fun onCreate(db: SupportSQLiteDatabase) {
//                            super.onCreate(db)
//                            Log.d("Database", "Database created successfully with ActivityLog support")
//                        }
//
//                        override fun onOpen(db: SupportSQLiteDatabase) {
//                            super.onOpen(db)
//                            // Enable foreign key constraints
//                            db.execSQL("PRAGMA foreign_keys=ON")
//                        }
//                    })
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}

//@Database(
//    entities = [
//        TransactionEntity::class,
//        CategoryEntity::class,
//        AccountEntity::class,
//        SubCategoryEntity::class
//    ],
//    version = 1,
//    exportSchema = false
//)
//@TypeConverters(Converters::class)
//abstract class TransactionDatabase : RoomDatabase() {
//
//    abstract fun transactionDao(): TransactionDao
//    abstract fun categoryDao(): CategoryDao
//    abstract fun accountDao(): AccountDao
//    abstract fun subCategoryDao(): SubCategoryDao
//
//    companion object {
//        const val DATABASE_NAME = "transaction_database"
//
//        @Volatile
//        private var INSTANCE: TransactionDatabase? = null
//
//        fun getDatabase(context: Context): TransactionDatabase {
//            Log.d("Database", "Initializing database")
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    TransactionDatabase::class.java,
//                    DATABASE_NAME
//                )
////                    .addCallback(object : Callback() {
////                        override fun onCreate(db: SupportSQLiteDatabase) {
////                            super.onCreate(db)
////                        }
////                    })
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}