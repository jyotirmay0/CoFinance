package com.finance.app.ui.screens.insights.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finance.app.domain.model.MonthlyTrend
import com.finance.app.ui.theme.FinanceTheme

@Composable
fun SpendingTrendChart(
    trends: List<MonthlyTrend>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("6M") }
    val filteredTrends = remember(trends, selectedFilter) {
        val count = when (selectedFilter) {
            "3M" -> 3
            "6M" -> 6
            "1Y" -> 12
            else -> 6
        }
        trends.takeLast(count)
    }

    val financeColors = FinanceTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(financeColors.cardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "MONTHLY\nSPENDING\nTREND",
                    style = MaterialTheme.typography.labelSmall,
                    color = financeColors.textSecondary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cash Outflow",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(4.dp)
            ) {
                listOf("3M", "6M", "1Y").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else financeColors.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Line Chart
        if (filteredTrends.isNotEmpty()) {
            val maxExpense = filteredTrends.maxOf { it.totalExpense }.coerceAtLeast(1.0)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (filteredTrends.size - 1).coerceAtLeast(1)

                    val path = Path()
                    val fillPath = Path()
                    
                    filteredTrends.forEachIndexed { index, trend ->
                        val x = index * spacing
                        val y = height - (trend.totalExpense.toFloat() / maxExpense.toFloat() * height * 0.8f) - (height * 0.1f)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            // Cubic-ish curve logic for smooth wave
                            val prevX = (index - 1) * spacing
                            val prevY = height - (filteredTrends[index-1].totalExpense.toFloat() / maxExpense.toFloat() * height * 0.8f) - (height * 0.1f)
                            
                            val controlX1 = prevX + (x - prevX) / 2
                            val controlX2 = prevX + (x - prevX) / 2
                            
                            path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                            fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        }
                    }
                    
                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Draw fill gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw main line
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw points (optional, per screenshot dots)
                    filteredTrends.forEachIndexed { index, _ ->
                        val x = index * spacing
                        val y = height - (filteredTrends[index].totalExpense.toFloat() / maxExpense.toFloat() * height * 0.8f) - (height * 0.1f)
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                filteredTrends.forEach { trend ->
                    Text(
                        text = trend.monthLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = financeColors.textSecondary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
