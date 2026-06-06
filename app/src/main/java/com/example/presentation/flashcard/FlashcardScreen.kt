package com.example.presentation.flashcard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VocabularyWordEntity
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.MinLishCard
import com.example.utils.TextToSpeechHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    viewModel: VocabularyViewModel,
    setId: Int,
    dueOnly: Boolean, // if true, study due cards only; if false, study all cards in set
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wordsList by viewModel.wordsInCurrentSet.collectAsState()

    // Filter cards to review using derivedStateOf for better performance
    val studySessionCards by remember {
        derivedStateOf {
            val now = System.currentTimeMillis()
            if (dueOnly) {
                wordsList.filter { it.nextReviewTimestamp <= now }
            } else {
                wordsList
            }
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isSessionFinished by remember { mutableStateOf(false) }
    var reviewsLoggedCount by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Học Flashcard SM-2", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (studySessionCards.isEmpty()) {
                NoCardsScreen(onBack = onBack)
            } else if (isSessionFinished) {
                FinishedSummaryScreen(
                    totalLogged = reviewsLoggedCount,
                    onBack = onBack
                )
            } else {
                val currentWord = studySessionCards[currentIndex]
                
                // Let's automatically read the English word when a new card loads!
                LaunchedEffect(currentIndex) {
                    ttsHelper.speak(currentWord.word)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Progress Indicator Area
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (currentIndex > 0) currentIndex-- },
                                enabled = currentIndex > 0,
                                modifier = Modifier.background(
                                    if (currentIndex > 0) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Trước đó")
                            }

                            Text(
                                text = "Card ${currentIndex + 1} / ${studySessionCards.size}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            IconButton(
                                onClick = { 
                                    advanceSession(
                                        size = studySessionCards.size,
                                        currentIndex = currentIndex,
                                        onIncrement = { currentIndex = it },
                                        onFinish = { isSessionFinished = true }
                                    )
                                },
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Tiếp theo")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = { (currentIndex.toFloat() + 1) / studySessionCards.size.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    }

                    // Interactive 3D Flipping Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FlippingFlashcard(
                            word = currentWord,
                            ttsHelper = ttsHelper
                        )
                    }

                    // SM-2 Spaced Repetition Rating Controller
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bạn có nhớ từ này không?",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(
                                onClick = {
                                    // Marking as Learned = 5 (Perfect) in SM-2 logic
                                    viewModel.reviewWordResponse(currentWord, 4)
                                    reviewsLoggedCount++
                                    advanceSession(
                                        size = studySessionCards.size,
                                        currentIndex = currentIndex,
                                        onIncrement = { currentIndex = it },
                                        onFinish = { isSessionFinished = true }
                                    )
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đã thuộc (Mastered)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Again Button (Red) - Reset interval, quality 1
                            ReviewButton(
                                text = "Lại (Again)",
                                subtitle = "Quên",
                                modifier = Modifier.weight(1f).testTag("rating_again_btn"),
                                containerColor = Color(0xFFFFEBEE),
                                contentColor = Color(0xFFC62828),
                                onClick = {
                                    viewModel.reviewWordResponse(currentWord, 1)
                                    reviewsLoggedCount++
                                    advanceSession(
                                        size = studySessionCards.size,
                                        currentIndex = currentIndex,
                                        onIncrement = { currentIndex = it },
                                        onFinish = { isSessionFinished = true }
                                    )
                                }
                            )

                            // Hard Button (Orange) - quality 2
                            ReviewButton(
                                text = "Khó (Hard)",
                                subtitle = "Lờ mờ",
                                modifier = Modifier.weight(1f).testTag("rating_hard_btn"),
                                containerColor = Color(0xFFFFF3E0),
                                contentColor = Color(0xFFE65100),
                                onClick = {
                                    viewModel.reviewWordResponse(currentWord, 2)
                                    reviewsLoggedCount++
                                    advanceSession(
                                        size = studySessionCards.size,
                                        currentIndex = currentIndex,
                                        onIncrement = { currentIndex = it },
                                        onFinish = { isSessionFinished = true }
                                    )
                                }
                            )

                            // Good Button (Blue) - quality 3
                            ReviewButton(
                                text = "Tốt (Good)",
                                subtitle = "Nhớ kịp",
                                modifier = Modifier.weight(1f).testTag("rating_good_btn"),
                                containerColor = Color(0xFFE3F2FD),
                                contentColor = Color(0xFF1565C0),
                                onClick = {
                                    viewModel.reviewWordResponse(currentWord, 3)
                                    reviewsLoggedCount++
                                    advanceSession(
                                        size = studySessionCards.size,
                                        currentIndex = currentIndex,
                                        onIncrement = { currentIndex = it },
                                        onFinish = { isSessionFinished = true }
                                    )
                                }
                            )

                            // Easy Button (Green) - quality 4
                            ReviewButton(
                                text = "Dễ (Easy)",
                                subtitle = "Nhớ ngay",
                                modifier = Modifier.weight(1f).testTag("rating_easy_btn"),
                                containerColor = Color(0xFFE8F5E9),
                                contentColor = Color(0xFF2E7D32),
                                onClick = {
                                    viewModel.reviewWordResponse(currentWord, 4)
                                    reviewsLoggedCount++
                                    advanceSession(
                                        size = studySessionCards.size,
                                        currentIndex = currentIndex,
                                        onIncrement = { currentIndex = it },
                                        onFinish = { isSessionFinished = true }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun advanceSession(
    size: Int,
    currentIndex: Int,
    onIncrement: (Int) -> Unit,
    onFinish: () -> Unit
) {
    if (currentIndex < size - 1) {
        onIncrement(currentIndex + 1)
    } else {
        onFinish()
    }
}

@Composable
fun FlippingFlashcard(
    word: VocabularyWordEntity,
    ttsHelper: TextToSpeechHelper
) {
    var isFlipped by remember(word) { mutableStateOf(false) }

    // 3D Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped }
            .testTag("flashcard_rendering_area"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (rotation <= 90f) {
            // Front Side
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mặt trước: Từ vựng",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    IconButton(
                        onClick = { ttsHelper.speak(word.word) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = "Speak pronunciation",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = word.pronunciation,
                        style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "👇 Nhấp rập để lật mặt sau",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            // Back Side (rotate the container again by 180 so it's readable!)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Mặt sau: Định nghĩa & Ví dụ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ý nghĩa:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = word.meaning,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (word.example.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ví dụ ngữ cảnh:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = word.example,
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (word.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "💡 Mẹo ghi nhớ:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = word.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "👇 Nhấp rập để lật lại mặt trước",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ReviewButton(
    text: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = text, fontWeight = FontWeight.Black, color = contentColor, fontSize = 12.sp)
            Text(text = subtitle, color = contentColor.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

@Composable
fun FinishedSummaryScreen(
    totalLogged: Int,
    onBack: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Celebration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "YAY! ĐÃ HOÀN THÀNH!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = "Bạn đã hoàn thành việc ôn luyện cho $totalLogged từ vựng tiếng Anh thành công dựa trên luật lặp khoảng cách SM-2 🚀",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("session_back_to_lobby_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Trở về sảnh học chính", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NoCardsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chúc mừng! Không còn thẻ đến hạn ôn tập",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bộ nhớ của bạn đối với phần này hiện đang ở đỉnh cao phong độ theo SM-2 logic.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 285.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Trở lại")
        }
    }
}
