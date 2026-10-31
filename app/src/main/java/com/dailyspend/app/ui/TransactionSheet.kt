package com.dailyspend.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dailyspend.app.data.Categories
import com.dailyspend.app.data.Transaction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionSheet(
    editing: Transaction?,
    currency: String,
    onSave: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
    onDismiss: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isIncome by remember { mutableStateOf(editing?.isIncome ?: false) }
    var amount by remember {
        mutableStateOf(
            editing?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: ""
        )
    }
    var title by remember { mutableStateOf(editing?.title ?: "") }
    var category by remember { mutableStateOf(editing?.category ?: "Food") }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var date by remember {
        mutableStateOf(
            editing?.let { Instant.ofEpochMilli(it.dateMillis).atZone(zone).toLocalDate() } ?: LocalDate.now()
        )
    }
    var showPicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val categories = if (isIncome) Categories.income else Categories.expense

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (editing == null) "Add transaction" else "Edit transaction",
                style = MaterialTheme.typography.titleLarge
            )

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("Expense" to false, "Income" to true).forEachIndexed { i, (label, inc) ->
                    SegmentedButton(
                        selected = isIncome == inc,
                        onClick = {
                            isIncome = inc
                            val list = if (inc) Categories.income else Categories.expense
                            if (list.none { it.first == category }) category = list.first().first
                        },
                        shape = SegmentedButtonDefaults.itemShape(i, 2)
                    ) { Text(label) }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' }; amountError = false },
                label = { Text("Amount ($currency)") },
                isError = amountError,
                supportingText = { if (amountError) Text("Enter an amount greater than 0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (name, emoji) ->
                    FilterChip(
                        selected = category == name,
                        onClick = { category = name },
                        label = { Text("$emoji $name") }
                    )
                }
            }

            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Date: " + date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")))
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        amountError = true
                    } else {
                        onSave(
                            Transaction(
                                id = editing?.id ?: 0,
                                title = title.trim().ifBlank { category },
                                amount = amt,
                                category = category,
                                isIncome = isIncome,
                                dateMillis = date.atStartOfDay(zone).toInstant().toEpochMilli(),
                                note = note.trim()
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }

            if (editing != null) {
                TextButton(
                    onClick = { onDelete(editing) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete") }
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }
}
