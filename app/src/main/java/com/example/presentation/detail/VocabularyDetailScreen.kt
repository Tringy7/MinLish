package com.example.presentation.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.VocabularySetEntity
import com.example.data.local.entity.VocabularyWordEntity
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.*
import com.example.utils.TextToSpeechHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyDetailScreen(
    viewModel: VocabularyViewModel,
    onBack: () -> Unit,
    onAddWordNeeded: (Int) -> Unit,
    onEditWordNeeded: (Int, Int) -> Unit,
    onStartStudy: (Int, Boolean) -> Unit, // (setId, dueOnly)
    modifier: Modifier = Modifier
) {
    val wordSet by viewModel.currentSet.collectAsState()
    val wordsList by viewModel.wordsInCurrentSet.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val csvData = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                            reader.readText()
                        }
                    }
                    csvData?.let { data ->
                        wordSet?.id?.let { setId ->
                            viewModel.importWords(setId, data)
                        }
                    }
                } catch (e: Exception) {
                    // Log error or show toast
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            wordSet?.id?.let { setId ->
                viewModel.exportWords(setId) { csvData ->
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                context.contentResolver.openOutputStream(it)?.use { output ->
                                    output.write(csvData.toByteArray())
                                }
                            }
                        } catch (e: Exception) {
                            // Log error or show toast
                        }
                    }
                }
            }
        }
    }

    // Initialize TTS helper cleanly from ViewModel
    val ttsHelper = remember { viewModel.getTtsHelper(context) }

    var wordQuery by remember { mutableStateOf("") }
    val filteredWords by remember {
        derivedStateOf {
            if (wordQuery.isBlank()) wordsList else {
                wordsList.filter {
                    it.word.contains(wordQuery, ignoreCase = true) ||
                    it.meaning.contains(wordQuery, ignoreCase = true)
                }
            }
        }
    }

    val dueWords by remember {
        derivedStateOf {
            wordsList.filter { it.nextReviewTimestamp <= System.currentTimeMillis() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(wordSet?.name ?: "Bộ từ vựng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về")
                    }
                },
                actions = {
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import CSV")
                    }
                    IconButton(onClick = { 
                        val fileName = "MinLish_${wordSet?.name ?: "Export"}.csv"
                        exportLauncher.launch(fileName) 
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { wordSet?.id?.let { onAddWordNeeded(it) } },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_word_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm từ mới")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Set Header Detail Banner
            wordSet?.let { activeSet ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = activeSet.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Display set tags
                    Row(modifier = Modifier.fillMaxWidth()) {
                        activeSet.tags.split(",").forEach { tag ->
                            if (tag.trim().isNotEmpty()) {
                                TagChip(tag = tag, containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Flashcard Control triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartStudy(activeSet.id, false) },
                            enabled = wordsList.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("study_all_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Học tất cả (${wordsList.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        ElevatedButton(
                            onClick = { onStartStudy(activeSet.id, true) },
                            enabled = dueWords.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("study_due_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ôn SM-2 (${dueWords.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Internal Search Filter
            OutlinedTextField(
                value = wordQuery,
                onValueChange = { wordQuery = it },
                placeholder = { Text("Tìm kiếm từ hoặc nghĩa...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("word_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Words items
            if (filteredWords.isEmpty()) {
                val phTitle = if (wordQuery.isNotBlank()) "Không tìm thấy từ" else "Kho rỗng"
                val phDesc = if (wordQuery.isNotBlank()) {
                    "Không tìm thấy kết quả từ vựng khớp với \"$wordQuery\"."
                } else {
                    "Bộ từ vựng này chưa có dữ liệu thẻ. Bấm Thêm để tạo mới!"
                }
                EmptyPlaceholder(
                    icon = Icons.Default.Book,
                    title = phTitle,
                    description = phDesc,
                    modifier = Modifier.weight(1f),
                    actionButton = if (wordQuery.isBlank()) {
                        {
                            Button(onClick = { wordSet?.id?.let { onAddWordNeeded(it) } }) {
                                Text("Thêm từ đầu tiên")
                            }
                        }
                    } else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = filteredWords, key = { it.id }) { wordItem ->
                        WordItemCard(
                            word = wordItem,
                            onSpeakClick = { ttsHelper.speak(wordItem.word) },
                            onFavoriteClick = { viewModel.toggleFavorite(wordItem) },
                            onEditClick = { onEditWordNeeded(wordItem.setId, wordItem.id) },
                            onDeleteClick = { viewModel.deleteWord(wordItem) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WordItemCard(
    word: VocabularyWordEntity,
    onSpeakClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDue = word.nextReviewTimestamp <= System.currentTimeMillis()
    
    MinLishCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = word.pronunciation,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = word.meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (word.example.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = word.example,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (word.descriptionEN.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = word.descriptionEN,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (word.collocations.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = word.collocations,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (word.relatedWords.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = word.relatedWords,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (word.note.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = Color(0xFFE65100).copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = word.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SM-2 Spaced Repetition Tags
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (word.repetitions == 0) {
                        TagChip(
                            tag = "Mới 🌱", 
                            containerColor = Color(0xFFE8F5E9), 
                            contentColor = Color(0xFF2E7D32)
                        )
                    } else {
                        TagChip(
                            tag = "Đã học (${word.repetitions} lần)", 
                            containerColor = Color(0xFFE1F5FE), 
                            contentColor = Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (isDue) {
                            TagChip(
                                tag = "Cần ôn tập ⏳", 
                                containerColor = Color(0xFFFFF3E0), 
                                contentColor = Color(0xFFE65100)
                            )
                        } else {
                            TagChip(
                                tag = "Chưa đến hạn", 
                                containerColor = Color(0xFFECEFF1), 
                                contentColor = Color(0xFF546E7A)
                            )
                        }
                    }
                }
            }

            // Utility buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSpeakClick) {
                        Icon(
                            imageVector = Icons.Outlined.VolumeUp,
                            contentDescription = "Phát âm tiếng Anh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (word.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Yêu thích",
                            tint = if (word.isFavorite) Color(0xFFFFBF00) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
