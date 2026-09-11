package com.cryptotrack.feature.portfolio.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.designsystem.theme.apexExtra

/**
 * Донат-диаграмма аллокации (макет Portfolio Overview: «Allocation — BTC 60%»).
 * Сегменты в порядке убывания доли; палитра — токен темы (читаема в light и dark).
 */
@Composable
fun AllocationDonut(
    allocations: List<AllocationUi>,
    modifier: Modifier = Modifier,
) {
    val segmentColors = MaterialTheme.colorScheme.apexExtra.donutSegments
    val idleColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                style = Stroke(width = strokeWidthPx),
            )
            return@Canvas
        }

        var startAngle = -90f
        allocations.forEachIndexed { index, allocation ->
            val sweep = (allocation.percent / 100.0 * 360.0).toFloat().coerceAtLeast(2f)
            drawArc(
                color = segmentColors[index % segmentColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx),
            )
            startAngle += sweep
        }
    }
}
