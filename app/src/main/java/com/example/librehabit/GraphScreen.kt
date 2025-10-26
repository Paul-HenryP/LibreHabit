package com.example.librehabit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    entries: List<WeightEntry>,
    unitSystem: UnitSystem,
    onNavigateUp: () -> Unit
) {
    val chartModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(entries, unitSystem) {
        val chartEntries = entries
            .reversed()
            .mapIndexed { index, entry ->
                val weightInSelectedUnit =
                    if (unitSystem == UnitSystem.IMPERIAL) entry.weight * 2.20462f else entry.weight
                entryOf(index.toFloat(), weightInSelectedUnit)
            }
        chartModelProducer.setEntries(chartEntries)
    }

    // X-axis formatter: shows fewer dates dynamically
    val bottomAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val totalEntries = entries.size
            val spacing = when {
                totalEntries <= 5 -> 1       // show all
                totalEntries <= 10 -> 2      // show every 2nd
                totalEntries <= 20 -> 3      // show every 3rd
                else -> 5                    // show every 5th
            }

            val index = value.toInt()
            if (index % spacing == 0) {
                entries.reversed().getOrNull(index)?.let { entry ->
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(entry.date)
                } ?: ""
            } else ""
        }

    // Y-axis formatter: 1 decimal place + fewer labels
    val startAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            String.format("%.1f", value)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weight History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (entries.size < 2) {
                Text(
                    text = "You need at least two entries to draw a graph.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Chart(
                    chart = lineChart(),
                    chartModelProducer = chartModelProducer,
                    startAxis = rememberStartAxis(
                        title = "Weight (${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"})",
                        valueFormatter = startAxisValueFormatter,
                        itemPlacer = AxisItemPlacer.Vertical.default(
                            maxItemCount = 5
                        )
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = bottomAxisValueFormatter,
                        guideline = null
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}
