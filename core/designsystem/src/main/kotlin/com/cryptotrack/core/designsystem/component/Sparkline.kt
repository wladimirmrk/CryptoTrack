package com.cryptotrack.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.designsystem.theme.priceColors

/**
 * Sparkline по спецификации design.md: без осей, толщина 1.5, скруглённые join'ы,
 * цвет = знак изменения цены (§1.5).
 */
@Composable
fun Sparkline(
    values: List<Double>,
    isUp: Boolean,
    modifier: Modifier = Modifier,
) {
    val strokeColor = if (isUp) {
        MaterialTheme.colorScheme.priceColors.up
    } else {
        MaterialTheme.colorScheme.priceColors.down
    }
    val strokeWidth = 1.5.dp
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minValue = values.min()
        val maxValue = values.max()
        val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range).toFloat() * size.height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}

/** Заглушка спарклайна для скелетона. */
@Composable
fun SparklineSkeleton(modifier: Modifier = Modifier) {
    Box(modifier = modifier.skeletonBackground())
}
