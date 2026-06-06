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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.EnglishLevel
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.MinLishCard

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

    val cefrLevels = EnglishLevel.entries
    val studyGoals = listOf("IELTS", "TOEIC", "Giao tiếp", "Công việc", "Du học", "Khác")

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
            // Avatar Card
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user?.avatarUrl?.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256" })
                            .crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    )
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
                                onClick = { viewModel.updateProfile(level, user?.learningGoal ?: "Giao tiếp") },
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
                                onClick = { viewModel.updateProfile(user?.englishLevel ?: EnglishLevel.B1, goal) },
                                label = { Text(goal) },
                                modifier = Modifier.weight(1f)
                            )
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
                    Switch(checked = dailyNotificationEnabled, onCheckedChange = { dailyNotificationEnabled = it })
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Báo từ vựng đến hạn ôn", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Thông báo khi có từ đến hạn SM-2.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reviewAlarmEnabled, onCheckedChange = { reviewAlarmEnabled = it })
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
