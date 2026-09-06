package com.cryptotrack.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Skeleton Loading по спецификации design.md: pulse-анимация на светло-сером фоне,
 * форма повторяет заменяемый компонент (§1.5).
 */
fun Modifier.skeletonBackground(shape: Shape? = null): Modifier = composed {
    val resolvedShape = shape ?: MaterialTheme.shapes.small
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton-alpha",
    )
    this
        .clip(resolvedShape)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .graphicsLayer(alpha = alpha)
}

/** Плоский скелет-блок заданного размера. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    androidx.compose.foundation.layout.Box(modifier = modifier.skeletonBackground(shape))
}
