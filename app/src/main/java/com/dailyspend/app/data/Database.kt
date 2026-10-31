package com.dailyspend.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.OnConflictStrategy
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val isIncome: Boolean,
    val dateMillis: Long,
    val note: String = ""
) {
    fun yearMonth(): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()))
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(t: Transaction)

    @Delete
    suspend fun delete(t: Transaction)
}

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TransactionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "dailyspend.db"
            ).build().also { instance = it }
        }
    }
}

object Categories {
    val expense = listOf(
        "Food" to "🍔", "Groceries" to "🛒", "Transport" to "🚌", "Bills" to "💡",
        "Rent" to "🏠", "Shopping" to "🛍️", "Health" to "💊", "Entertainment" to "🎬",
        "Education" to "📚", "Travel" to "✈️", "Other" to "📦"
    )
    val income = listOf(
        "Salary" to "💼", "Business" to "🏪", "Gift" to "🎁", "Interest" to "🏦", "Other" to "💰"
    )

    fun emoji(category: String, isIncome: Boolean): String =
        (if (isIncome) income else expense).firstOrNull { it.first == category }?.second ?: "📦"
}
