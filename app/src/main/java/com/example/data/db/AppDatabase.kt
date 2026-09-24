package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Categories
import com.example.data.model.ProductEntity
import com.example.data.model.Roles
import com.example.data.model.StockHistoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        StockHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionItemDao(): TransactionItemDao
    abstract fun stockHistoryDao(): StockHistoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dimzone_pos_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val productDao = database.productDao()
            val userDao = database.userDao()

            if (userDao.getCount() == 0) {
                userDao.insertAll(
                    listOf(
                        UserEntity(
                            username = "admin",
                            fullName = "DIMZONE Admin",
                            role = Roles.ADMIN,
                            pin = "1234"
                        ),
                        UserEntity(
                            username = "kasir1",
                            fullName = "Siti Kasir",
                            role = Roles.KASIR,
                            pin = "1111"
                        ),
                        UserEntity(
                            username = "kasir2",
                            fullName = "Rian Kasir",
                            role = Roles.KASIR,
                            pin = "2222"
                        )
                    )
                )
            }

            if (productDao.getCount() == 0) {
                productDao.insertAll(
                    listOf(
                        ProductEntity(
                            name = "Dimsum Ori",
                            category = Categories.ORI,
                            price = 18000.0,
                            costPrice = 11000.0,
                            stock = 50,
                            minStockAlert = 10,
                            imageUrl = "dimsum_ori",
                            description = "Dimsum kukus lembut isi ayam udang premium isi 4 pcs"
                        ),
                        ProductEntity(
                            name = "Dimsum Mentai",
                            category = Categories.MENTAI,
                            price = 23000.0,
                            costPrice = 14000.0,
                            stock = 40,
                            minStockAlert = 10,
                            imageUrl = "dimsum_mentai",
                            description = "Dimsum lembut dengan topping saus mentai gurih dan tobiko torched"
                        ),
                        ProductEntity(
                            name = "Dimsum Mix",
                            category = Categories.MIX,
                            price = 22000.0,
                            costPrice = 13000.0,
                            stock = 35,
                            minStockAlert = 8,
                            imageUrl = "dimsum_mix",
                            description = "Kombinasi 4 varian favorit: Kepiting, Jamur, Nori, dan Daging Sapi"
                        ),
                        ProductEntity(
                            name = "Dimsum Bakar",
                            category = Categories.BAKAR,
                            price = 20000.0,
                            costPrice = 12000.0,
                            stock = 45,
                            minStockAlert = 10,
                            imageUrl = "dimsum_bakar",
                            description = "Dimsum bakar saus barbeque pedas manis dengan aroma smokey"
                        ),
                        ProductEntity(
                            name = "Add On Hot Lava",
                            category = Categories.ADDON,
                            price = 4000.0,
                            costPrice = 2000.0,
                            stock = 100,
                            minStockAlert = 15,
                            imageUrl = "addon_lava",
                            description = "Ekstra saus pedas hot lava membakar lidah"
                        ),
                        ProductEntity(
                            name = "Add On Chili Oil",
                            category = Categories.ADDON,
                            price = 3000.0,
                            costPrice = 1500.0,
                            stock = 100,
                            minStockAlert = 15,
                            imageUrl = "addon_chili",
                            description = "Minyak cabai renyah aromatik khas DIMZONE"
                        ),
                        ProductEntity(
                            name = "Add On Nori",
                            category = Categories.ADDON,
                            price = 3000.0,
                            costPrice = 1200.0,
                            stock = 80,
                            minStockAlert = 15,
                            imageUrl = "addon_nori",
                            description = "Taburan rumput laut kering renyah gurih"
                        ),
                        ProductEntity(
                            name = "Add On Mozarella",
                            category = Categories.ADDON,
                            price = 5000.0,
                            costPrice = 2500.0,
                            stock = 60,
                            minStockAlert = 12,
                            imageUrl = "addon_moza",
                            description = "Lelehan keju mozarella tebal dan melar"
                        )
                    )
                )
            }
        }
    }
}
