package com.dailyspend.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailyspend.app.ExpenseViewModel
import com.dailyspend.app.data.Categories

@Composable
fun StatsScreen(vm: ExpenseViewModel, padding: PaddingValues) {
    val all by vm.transactions.collectAsStateWithLifecycle()
    val month by vm.month.collectAsStateWithLifecycle()
    val cur by vm.currency.collectAsStateWithLifecycle()

    val expenses = remember(all, month) { all.filter { !it.isIncome && it.yearMonth() == month } }
    val total = expenses.sumOf { it.amount }
    val byCategory = remember(expenses) {
        expenses.groupBy { it.category }
            .map { (c, l) -> c to l.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }
    val months = remember(month) { (5 downTo 0L).map { month.minusMonths(it) } }
    val totals = remember(all, month) {
        months.map { m -> all.filter { !it.isIncome && it.yearMonth() == m }.sumOf { it.amount } }
    }
    val maxTotal = (totals.maxOrNull() ?: 0.0).coerceAtLeast(1.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { MonthSelector(month, { vm.shiftMonth(-1) }, { vm.shiftMonth(1) }) }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Spending – last 6 months", style = MaterialTheme.typography.titleSmall)
                    Row(
                        Modifier.fillMaxWidth().height(170.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        months.forEachIndexed { i, m ->
                            Column(
                                Modifier.weight(1f).fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height((120 * (totals[i] / maxTotal)).dp.coerceAtLeast(2.dp))
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (m == month) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                        )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(m.shortLabel(), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("By category · ${money(cur, total)}", style = MaterialTheme.typography.titleSmall)
        }
        if (byCategory.isEmpty()) {
            item { Text("No expenses this month.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(byCategory.size) { i ->
            val (cat, sum) = byCategory[i]
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${Categories.emoji(cat, false)}  $cat")
                    Text(
                        "${money(cur, sum)}  (${(sum / total * 100).toInt()}%)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { (sum / total).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
