package com.example.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.GlassTitle
import com.example.presentation.components.MinLishCard
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: VocabularyViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tiến Trình Học Tập", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassTitle(
                title = "Thống Kê",
                subtitle = "Thông số học & Lịch sử ghi nhớ SM-2"
            )

            // Dynamic grid of scorecards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMicroCard(
                    title = "Tổng số từ",
                    value = "${stats.totalWordsCount}",
                    icon = Icons.Default.MenuBook,
                    tint = MaterialTheme.colorScheme.primary,
                    bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                )

                StatMicroCard(
                    title = "Từ đã học",
                    value = "${stats.learnedWordsCount}",
                    icon = Icons.Default.CheckCircle,
                    tint = Color(0xFF2E7D32),
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMicroCard(
                    title = "Tỉ lệ đúng",
                    value = "${stats.retentionRate}%",
                    icon = Icons.Default.Memory,
                    tint = Color(0xFFD81B60),
                    bgColor = Color(0xFFFCE4EC),
                    modifier = Modifier.weight(1f)
                )

                StatMicroCard(
                    title = "Ngày liên tiếp",
                    value = "${stats.currentStreak} 🔥",
                    icon = Icons.Default.Whatshot,
                    tint = Color(0xFFE65100),
                    bgColor = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                )
            }

            // Word Review Targets
            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Chờ ôn tập hôm nay:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.dueTodayCount} từ tiếng Anh",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = if (stats.dueTodayCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (stats.dueTodayCount > 0) Color(0xFFFFE0B2) else Color(0xFFC8E6C9),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (stats.dueTodayCount > 0) Icons.Default.HourglassEmpty else Icons.Default.Done,
                            contentDescription = null,
                            tint = if (stats.dueTodayCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // High Fidelity Charting using Canvas
            MinLishCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progress_chart_container")
            ) {
                Text(
                    text = "Hoạt động học 7 ngày gần nhất",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Số lượt ôn tập & kiểm tra từ vựng",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (stats.dailyActivities.isEmpty() || stats.dailyActivities.all { it.count == 0 }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có hoạt động học nào được ghi nhận. Hãy ôn từ ngay hôm nay!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    StudyActivityChart(
                        activities = stats.dailyActivities,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        secondaryColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatMicroCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(bgColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StudyActivityChart(
    activities: List<com.example.presentation.DailyActivity>,
    primaryColor: Color,
    secondaryColor: Color
) {
    val maxCount = remember(activities) {
        max(5, activities.maxOf { it.count })
    }

    val canvasHeight = 160.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(canvasHeight + 40.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
        ) {
            val width = size.width
            val height = size.height
            val spacing = width / (activities.size)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            // Draw horizontal dotted gridlines
            val gridLines = 3
            for (i in 0..gridLines) {
                val gridY = (height / gridLines) * i
                drawLine(
                    color = Color.Gray.copy(alpha = 0.15f),
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bars
            activities.forEachIndexed { index, activity ->
                val barWidth = 36.dp.toPx()
                val fraction = activity.count.toFloat() / maxCount.toFloat()
                
                // Set bounds
                val drawHeight = (height * 0.82f) * fraction
                val x = (spacing * index) + (spacing / 2) - (barWidth / 2)
                val y = height - drawHeight - 16.dp.toPx()

                // Draw solid background round bar
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.9f),
                            secondaryColor.copy(alpha = 0.6f)
                        )
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, drawHeight),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                )

                // Render reviews count text above bars
                if (activity.count > 0) {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            "${activity.count}",
                            x + (barWidth / 2),
                            y - 6.dp.toPx(),
                            paint.apply {
                                color = android.graphics.Color.DKGRAY
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                            }
                        )
                    }
                }

                // Render weekday label under bars
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        activity.dayLabel,
                        x + (barWidth / 2),
                        height,
                        paint.apply {
                            color = android.graphics.Color.GRAY
                            typeface = android.graphics.Typeface.DEFAULT
                        }
                    )
                }
            }
        }
    }
}
