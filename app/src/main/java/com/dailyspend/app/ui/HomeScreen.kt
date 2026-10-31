package com.dailyspend.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailyspend.app.ExpenseViewModel
import com.dailyspend.app.data.Categories
import com.dailyspend.app.data.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MonthSelector(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month") }
        Text(month.label(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next month") }
    }
}

@Composable
fun HomeScreen(vm: ExpenseViewModel, padding: PaddingValues, onEdit: (Transaction) -> Unit) {
    val all by vm.transactions.collectAsStateWithLifecycle()
    val month by vm.month.collectAsStateWithLifecycle()
    val budget by vm.budget.collectAsStateWithLifecycle()
    val cur by vm.currency.collectAsStateWithLifecycle()

    val items = remember(all, month) { all.filter { it.yearMonth() == month } }
    val income = items.filter { it.isIncome }.sumOf { it.amount }
    val expense = items.filter { !it.isIncome }.sumOf { it.amount }
    val zone = ZoneId.systemDefault()
    val grouped = remember(items) {
        items.groupBy { Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate() }
    }
    val dayFmt = remember { DateTimeFormatter.ofPattern("EEE, d MMM") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item { MonthSelector(month, { vm.shiftMonth(-1) }, { vm.shiftMonth(1) }) }
        item {
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Balance this month", style = MaterialTheme.typography.labelLarge)
                    Text(
                        money(cur, income - expense),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Income", style = MaterialTheme.typography.labelMedium)
                            Text(money(cur, income), color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Spent", style = MaterialTheme.typography.labelMedium)
                            Text(money(cur, expense), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (budget > 0) {
                        val frac = (expense / budget).toFloat()
                        LinearProgressIndicator(
                            progress = { frac.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (frac >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            if (expense > budget) "Over budget by ${money(cur, expense - budget)}"
                            else "${money(cur, budget - expense)} left of ${money(cur, budget)} budget",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        if (items.isEmpty()) {
            item {
                Text(
                    "No transactions this month.\nTap + to add one.",
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        grouped.forEach { (date: LocalDate, list) ->
            item(key = "h$date") {
                Text(
                    date.format(dayFmt),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(list, key = { it.id }) { t ->
                ListItem(
                    modifier = Modifier.clickable { onEdit(t) },
                    leadingContent = { Text(Categories.emoji(t.category, t.isIncome), fontSize = 26.sp) },
                    headlineContent = { Text(t.title) },
                    supportingContent = {
                        Text(if (t.note.isBlank()) t.category else "${t.category} · ${t.note}", maxLines = 1)
                    },
                    trailingContent = {
                        Text(
                            (if (t.isIncome) "+" else "-") + money(cur, t.amount),
                            color = if (t.isIncome) IncomeGreen else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }
    }
}
