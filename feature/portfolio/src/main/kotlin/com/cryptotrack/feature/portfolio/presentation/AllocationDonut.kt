package com.cryptotrack.feature.portfolio.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val donutColors = listOf(
    Color(0xFF0052FF),
    Color(0xFF003EC7),
    Color(0xFF7B9BFF),
    Color(0xFF505F76),
    Color(0xFFB7C4FF),
    Color(0xFF952200),
)

/**
 * Донат-диаграмма аллокации (макет Portfolio Overview: «Allocation — BTC 60%»).
 * Сегменты в порядке убывания доли.
 */
@Composable
fun AllocationDonut(
    allocations: List<AllocationUi>,
    modifier: Modifier = Modifier,
) {
    val idleColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
    val stroke = 22.dp
    Canvas(modifier = modifier.size(120.dp)) {
        val strokeWidthPx = stroke.toPx()
        val inset = strokeWidthPx / 2
        val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
        val topLeft = Offset(inset, inset)

        if (allocations.isEmpty()) {
            drawArc(
                color = idleColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
            )
            return@Canvas
        }

        var startAngle = -90f
        allocations.forEachIndexed { index, allocation ->
            val sweep = (allocation.percent / 100.0 * 360.0).toFloat().coerceAtLeast(2f)
            drawArc(
                color = donutColors[index % donutColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
            )
            startAngle += sweep
        }
    }
}
