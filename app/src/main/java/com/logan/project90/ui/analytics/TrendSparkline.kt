package com.logan.project90.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TrendSparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        if (values.size < 2) return@Canvas

        val minValue = values.minOrNull() ?: 0.0
        val maxValue = values.maxOrNull() ?: 0.0
        val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val stepX = if (values.size == 1) 0f else size.width / (values.size - 1)
        val path = Path()

        values.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = ((value - minValue) / range).toFloat()
            val y = size.height - (normalized * size.height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3f)
        )

        val lastX = stepX * (values.lastIndex)
        val lastNormalized = ((values.last() - minValue) / range).toFloat()
        val lastY = size.height - (lastNormalized * size.height)
        drawCircle(
            color = color,
            radius = 4f,
            center = Offset(lastX, lastY)
        )
    }
}
