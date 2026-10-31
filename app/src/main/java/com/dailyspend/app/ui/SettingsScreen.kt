package com.dailyspend.app.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailyspend.app.ExpenseViewModel

@Composable
fun SettingsScreen(vm: ExpenseViewModel, padding: PaddingValues) {
    val budget by vm.budget.collectAsStateWithLifecycle()
    val cur by vm.currency.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    var budgetText by remember(budget) { mutableStateOf(if (budget > 0) budget.toLong().toString() else "") }
    var curText by remember(cur) { mutableStateOf(cur) }

    Column(
        Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Monthly budget", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it.filter(Char::isDigit) },
            label = { Text("Budget amount (0 = off)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Currency symbol", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = curText,
            onValueChange = { curText = it.take(4) },
            label = { Text("e.g. ₹  $  €  £") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                vm.setBudget(budgetText.toDoubleOrNull() ?: 0.0)
                vm.setCurrency(curText.ifBlank { "₹" })
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save settings") }

        HorizontalDivider()

        Text("Your data", style = MaterialTheme.typography.titleMedium)
        Text(
            "All transactions are stored only on this phone. Export them as a CSV file to open in Excel or Google Sheets.",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedButton(
            onClick = { ctx.startActivity(Intent.createChooser(vm.buildExportIntent(), "Export transactions")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Export to CSV") }
    }
}
