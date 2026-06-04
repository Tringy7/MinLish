package com.example.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.GlassTitle
import com.example.presentation.components.MinLishCard
import com.example.utils.ReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: VocabularyViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userState.collectAsState()
    val context = LocalContext.current

    var dailyNotificationEnabled by rememberSaveable { mutableStateOf(true) }
    var reviewAlarmEnabled by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ Sơ Cá Nhân", fontWeight = FontWeight.Bold) },
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
            // Personal Avatar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user?.avatarUrl?.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256" })
                            .crossfade(true)
                            .build(),
                        contentDescription = "User avatar profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = user?.name ?: "Hải Đăng",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user?.email ?: "learner@minlish.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Target english level config
            Text(
                text = "CÀI ĐẶT TRÌNH ĐỘ TIẾNG ANH",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                val levels = listOf(
                    "A2 - Elementary",
                    "B1 - Pre-Intermediate",
                    "B2 - Upper Intermediate",
                    "C1 - Advanced"
                )

                levels.forEach { level ->
                    val isSelected = user?.englishLevel == level
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateEnglishLevel(level) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = level,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active level choice",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (level != levels.last()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }

            // Notifications configuration (WorkManager Simulator triggers)
            Text(
                text = "THÔNG BÁO NHẮC HỌC (SM-2 ALARM)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                // Daily notifications switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nhắc học mỗi ngày",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Dùng WorkManager nhắc bạn ôn tập hàng ngày lúc 20:00.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dailyNotificationEnabled,
                        onCheckedChange = {
                            dailyNotificationEnabled = it
                            if (it) {
                                ReminderManager.scheduleDailyReminder(context)
                                Toast.makeText(context, "Đã bật: Lịch nhắc nhở hàng ngày (20:00).", Toast.LENGTH_SHORT).show()
                            } else {
                                ReminderManager.cancelDailyReminder(context)
                                Toast.makeText(context, "Đã tắt lịch nhắc nhở hàng ngày.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("switch_daily_reminder")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Words due alarm switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Báo từ vựng đến hạn ôn",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Báo động định kỳ khi có từ vựng đến thời hạn lặp lại của SM-2.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = reviewAlarmEnabled,
                        onCheckedChange = {
                            reviewAlarmEnabled = it
                            if (it) {
                                ReminderManager.scheduleReviewReminder(context)
                                Toast.makeText(context, "Đã bật: Thông báo khi có từ đến hạn ôn tập.", Toast.LENGTH_SHORT).show()
                            } else {
                                ReminderManager.cancelReviewReminder(context)
                                Toast.makeText(context, "Đã tắt cảnh báo từ đến hạn.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("switch_due_reminder")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout execution
            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng Xuất", fontWeight = FontWeight.Bold)
            }
        }
    }
}
