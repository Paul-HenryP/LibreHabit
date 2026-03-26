package com.paulhenryp.librehabit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.paulhenryp.librehabit.ui.components.EmptyState
import java.text.SimpleDateFormat
import java.util.*

private const val DATA_POINT_VISIBILITY_THRESHOLD = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    entries: List<WeightEntry>,
    unitSystem: UnitSystem,
    targetWeight: Float,
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

    val dataLabel = textComponent(
        color = MaterialTheme.colorScheme.onSurface,
        textSize = 10.sp,
        background = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surfaceVariant),
        padding = dimensionsOf(horizontal = 6.dp, vertical = 2.dp)
    )
    val dataPoint = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.primary)

    val lineSpec = LineChart.LineSpec(
        lineColor = MaterialTheme.colorScheme.primary.toArgb(),
        point = if (entries.size <= DATA_POINT_VISIBILITY_THRESHOLD) dataPoint else null,
        dataLabel = if (entries.size <= DATA_POINT_VISIBILITY_THRESHOLD) dataLabel else null,
        dataLabelValueFormatter = object : ValueFormatter {
            override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
                return String.format(Locale.US, "%.1f", value)
            }
        }
    )

    val targetInSelectedUnit = if (unitSystem == UnitSystem.IMPERIAL) targetWeight * 2.20462f else targetWeight

    val thresholdLineComp = shapeComponent(color = MaterialTheme.colorScheme.tertiary)
    val thresholdLabelComp = textComponent(
        color = MaterialTheme.colorScheme.onSurface,
        background = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.tertiaryContainer),
        padding = dimensionsOf(horizontal = 8.dp, vertical = 2.dp),
        margins = dimensionsOf(horizontal = 4.dp)
    )

    val thresholdLine = remember(targetInSelectedUnit, thresholdLineComp, thresholdLabelComp) {
        if (targetWeight > 0f) {
            ThresholdLine(
                thresholdValue = targetInSelectedUnit,
                thresholdLabel = String.format(Locale.US, "Target: %.1f", targetInSelectedUnit),
                lineComponent = thresholdLineComp,
                labelComponent = thresholdLabelComp
            )
        } else null
    }

    val decorations = if (thresholdLine != null) listOf(thresholdLine) else emptyList()

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
                EmptyState(
                    message = "Not enough data.\nAdd at least two entries to see your progress graph.",
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ProvideChartStyle(chartStyle = chartStyle) {
                    Chart(
                        chart = lineChart(
                            lines = listOf(lineSpec),
                            decorations = decorations
                        ),
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
}