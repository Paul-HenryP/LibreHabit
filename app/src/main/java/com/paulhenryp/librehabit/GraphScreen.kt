package com.paulhenryp.librehabit

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
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    entries: List<WeightEntry>,
    unitSystem: UnitSystem,
    onNavigateUp: () -> Unit
) {
    val chartModelProducer = remember { ChartEntryModelProducer() }
    val chartStyle = m3ChartStyle()

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

    val bottomAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val totalEntries = entries.size
            val spacing = when {
                totalEntries <= 5 -> 1
                totalEntries <= 10 -> 2
                totalEntries <= 20 -> 3
                else -> 5
            }

            val index = value.toInt()
            if (index % spacing == 0) {
                entries.reversed().getOrNull(index)?.let { entry ->
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(entry.date)
                } ?: ""
            } else ""
        }

    val startAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            String.format(Locale.US, "%.1f", value)
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
                ProvideChartStyle(chartStyle = chartStyle) {
                    Chart(
                        chart = lineChart(),
                        chartModelProducer = chartModelProducer,
                        startAxis = rememberStartAxis(
                            title = "Weight (${if (unitSystem == UnitSystem.METRIC) "kg" else "lbs"})",
                            valueFormatter = startAxisValueFormatter,
                            itemPlacer = AxisItemPlacer.Vertical.default(
                                maxItemCount = 10
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
}