package com.dailyspend.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailyspend.app.data.Transaction
import com.dailyspend.app.ui.DailySpendTheme
import com.dailyspend.app.ui.HomeScreen
import com.dailyspend.app.ui.SettingsScreen
import com.dailyspend.app.ui.StatsScreen
import com.dailyspend.app.ui.TransactionSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DailySpendTheme { MainScreen() } }
    }
}

private data class Tab(val title: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: ExpenseViewModel = viewModel()) {
    val tabs = listOf(
        Tab("Home", Icons.Filled.Home),
        Tab("Stats", Icons.Filled.BarChart),
        Tab("Settings", Icons.Filled.Settings)
    )
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Transaction?>(null) }
    val currency by vm.currency.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(tabs[selected].title.let { if (it == "Home") "DailySpend" else it }) }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, tab ->
                    NavigationBarItem(
                        selected = selected == i,
                        onClick = { selected = i },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selected == 0) {
                FloatingActionButton(onClick = { editing = null; showForm = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                }
            }
        }
    ) { padding: PaddingValues ->
        when (selected) {
            0 -> HomeScreen(vm, padding) { editing = it; showForm = true }
            1 -> StatsScreen(vm, padding)
            else -> SettingsScreen(vm, padding)
        }
    }

    if (showForm) {
        TransactionSheet(
            editing = editing,
            currency = currency,
            onSave = { vm.save(it); showForm = false },
            onDelete = { vm.delete(it); showForm = false },
            onDismiss = { showForm = false }
        )
    }
}
