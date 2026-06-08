package com.example.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.EnglishLevel
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.MinLishCard
import com.example.utils.ReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: VocabularyViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.userState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var dailyNotificationEnabled by rememberSaveable { mutableStateOf(true) }
    var reviewAlarmEnabled by rememberSaveable { mutableStateOf(true) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // In a real app, you would copy this to internal storage
            viewModel.updateProfile(avatarUrl = it.toString())
        }
    }

    val cefrLevels = EnglishLevel.entries
    val studyGoals = listOf("IELTS", "TOEIC", "Giao tiếp", "Công việc", "Du học", "Khác")

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 24.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = "CÀI ĐẶT CÁ NHÂN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Hồ sơ của bạn",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Avatar Card
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user?.avatarUrl?.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256" })
                                .crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .clickable { imagePickerLauncher.launch("image/*") }
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(32.dp)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile Image",
                                modifier = Modifier.padding(6.dp).size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = user?.name ?: "User", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(text = user?.email ?: "email", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // CEFR Level Section
            Text(text = "TRÌNH ĐỘ TIẾNG ANH (CEFR)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                cefrLevels.chunked(3).forEach { rowLevels ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowLevels.forEach { level ->
                            val isSelected = user?.englishLevel == level
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateProfile(level = level) },
                                label = { Text(level.label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Learning Goal Section
            Text(text = "MỤC TIÊU HỌC TẬP", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                studyGoals.chunked(2).forEach { rowGoals ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowGoals.forEach { goal ->
                            val isSelected = user?.learningGoal == goal
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateProfile(goal = goal) },
                                label = { Text(goal) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Daily Target Section
            Text(text = "MỤC TIÊU HÀNG NGÀY", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                var isEditingGoal by remember { mutableStateOf(false) }
                var goalInput by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Số từ mới cần ôn", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    
                    if (isEditingGoal) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = goalInput,
                                onValueChange = { if (it.all { char -> char.isDigit() }) goalInput = it },
                                modifier = Modifier.width(80.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                            IconButton(onClick = {
                                val newGoal = goalInput.toIntOrNull() ?: 20
                                viewModel.updateProfile(dailyGoalWords = newGoal)
                                isEditingGoal = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { 
                            goalInput = (user?.dailyGoalWords ?: 20).toString()
                            isEditingGoal = true 
                        }) {
                            Text(
                                text = "${user?.dailyGoalWords ?: 20}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Notifications
            Text(text = "CÀI ĐẶT THÔNG BÁO", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            MinLishCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Nhắc học mỗi ngày", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "WorkManager nhắc bạn lúc 20:00.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Báo từ vựng đến hạn ôn", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Thông báo khi có từ đến hạn.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Button(
                onClick = { viewModel.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
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
