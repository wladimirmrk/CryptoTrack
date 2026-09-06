package com.cryptotrack.feature.coindetails.presentation.chart

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.designsystem.theme.priceColors

/**
 * График цены для Coin Details (ADR-007): area/line Canvas без осей —
 * заливка градиентом под линией, цвет по знаку изменения за период.
 */
@Composable
fun PriceChart(
    values: List<Double>,
    isUp: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.priceColors
    val lineColor = if (isUp) colors.up else colors.down
    val fillColor = if (isUp) colors.up else colors.down
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minValue = values.min()
        val maxValue = values.max()
        val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val stepX = size.width / (values.size - 1)

        fun yOf(value: Double): Float =
            size.height - ((value - minValue) / range).toFloat() * size.height

        val linePath = Path()
        val areaPath = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = yOf(value)
            if (index == 0) {
                linePath.moveTo(x, y)
                areaPath.moveTo(x, size.height)
                areaPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                areaPath.lineTo(x, y)
            }
        }
        areaPath.lineTo(size.width, size.height)
        areaPath.close()

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.25f), fillColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = size.height,
            ),
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
