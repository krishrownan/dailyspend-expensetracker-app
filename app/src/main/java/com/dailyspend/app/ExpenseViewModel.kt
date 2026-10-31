package com.dailyspend.app

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailyspend.app.data.AppDatabase
import com.dailyspend.app.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class ExpenseViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val transactions: StateFlow<List<Transaction>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val month = MutableStateFlow(YearMonth.now())
    val budget = MutableStateFlow(prefs.getFloat("budget", 0f).toDouble())
    val currency = MutableStateFlow(prefs.getString("currency", "₹") ?: "₹")

    fun shiftMonth(delta: Long) {
        month.value = month.value.plusMonths(delta)
    }

    fun save(t: Transaction) = viewModelScope.launch { dao.upsert(t) }
    fun delete(t: Transaction) = viewModelScope.launch { dao.delete(t) }

    fun setBudget(value: Double) {
        budget.value = value
        prefs.edit().putFloat("budget", value.toFloat()).apply()
    }

    fun setCurrency(symbol: String) {
        currency.value = symbol
        prefs.edit().putString("currency", symbol).apply()
    }

    /** Writes all transactions to a CSV file and returns a share intent. */
    fun buildExportIntent(): Intent {
        val ctx = getApplication<Application>()
        val dir = File(ctx.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "dailyspend-export.csv")
        val zone = ZoneId.systemDefault()
        val sb = StringBuilder("Date,Type,Category,Title,Amount,Note\n")
        transactions.value.forEach { t ->
            val date = Instant.ofEpochMilli(t.dateMillis).atZone(zone).toLocalDate()
            fun esc(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
            sb.append("$date,${if (t.isIncome) "Income" else "Expense"},${esc(t.category)},${esc(t.title)},${t.amount},${esc(t.note)}\n")
        }
        file.writeText(sb.toString())
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
