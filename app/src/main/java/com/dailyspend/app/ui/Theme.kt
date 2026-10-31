package com.dailyspend.app.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.time.YearMonth
import java.time.format.DateTimeFormatter

val IncomeGreen = Color(0xFF2E7D32)

@Composable
fun DailySpendTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme(primary = Color(0xFF4DB6AC))
        else -> lightColorScheme(primary = Color(0xFF00695C))
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

fun money(symbol: String, amount: Double): String = "$symbol${"%,.2f".format(amount)}"

fun YearMonth.label(): String = format(DateTimeFormatter.ofPattern("MMMM yyyy"))
fun YearMonth.shortLabel(): String = format(DateTimeFormatter.ofPattern("MMM"))
