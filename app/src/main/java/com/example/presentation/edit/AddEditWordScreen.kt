package com.example.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.VocabularyWordEntity
import com.example.presentation.VocabularyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWordScreen(
    viewModel: VocabularyViewModel,
    setId: Int,
    wordId: Int?, // if null, we are in Add mode; if not null, we are in Edit mode
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var word by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var descriptionEN by remember { mutableStateOf("") }
    var collocations by remember { mutableStateOf("") }
    var relatedWords by remember { mutableStateOf("") }

    var existingWordEntity by remember { mutableStateOf<VocabularyWordEntity?>(null) }

    // Load existing word state if in Edit mode
    LaunchedEffect(wordId) {
        if (wordId != null) {
            val loaded = viewModel.wordsInCurrentSet.value.firstOrNull { it.id == wordId }
            if (loaded != null) {
                existingWordEntity = loaded
                word = loaded.word
                pronunciation = loaded.pronunciation
                meaning = loaded.meaning
                example = loaded.example
                note = loaded.note
                descriptionEN = loaded.descriptionEN
                collocations = loaded.collocations
                relatedWords = loaded.relatedWords
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (wordId == null) "Thêm Từ Vựng Mới" else "Chỉnh Sửa Từ Vựng",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("Từ vựng tiếng Anh (*)") },
                placeholder = { Text("Ví dụ: Pragmatic") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_word_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = pronunciation,
                onValueChange = { pronunciation = it },
                label = { Text("Phiên âm IPA") },
                placeholder = { Text("Ví dụ: /præɡˈmæt.ɪk/") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_ipa_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                label = { Text("Nghĩa tiếng Việt (*)") },
                placeholder = { Text("Ví dụ: Thực tiễn, thực tế") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_meaning_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Ví dụ đặt câu") },
                placeholder = { Text("Ví dụ: He takes a pragmatic approach.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_example_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )
            // English Description
            OutlinedTextField(
                value = descriptionEN,
                onValueChange = { descriptionEN = it },
                label = { Text("Mô tả tiếng Anh") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            // Collocations
            OutlinedTextField(
                value = collocations,
                onValueChange = { collocations = it },
                label = { Text("Collocations (Các cụm từ đi kèm)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            // Related Words
            OutlinedTextField(
                value = relatedWords,
                onValueChange = { relatedWords = it },
                label = { Text("Từ liên quan (Đồng nghĩa/Trái nghĩa)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú bổ sung (mẹo nhớ từ)") },
                placeholder = { Text("Ví dụ: Trái nghĩa với theoretical.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_note_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank()) {
                        if (wordId == null) {
                            viewModel.addWord(
                                setId = setId,
                                wordTxt = word.trim(),
                                ipa = pronunciation.trim(),
                                meaningTxt = meaning.trim(),
                                exampleTxt = example.trim(),
                                noteTxt = note.trim(),
                                descriptionEN = descriptionEN.trim(),
                                collocations = collocations.trim(),
                                relatedWords = relatedWords.trim()
                            )
                        } else {
                            existingWordEntity?.let { oldWord ->
                                viewModel.editWord(
                                    oldWord.copy(
                                        word = word.trim(),
                                        pronunciation = pronunciation.trim(),
                                        meaning = meaning.trim(),
                                        example = example.trim(),
                                        note = note.trim(),
                                        descriptionEN = descriptionEN.trim(),
                                        collocations = collocations.trim(),
                                        relatedWords = relatedWords.trim()

                                    )
                                )
                            }
                        }
                        onBack()
                    }
                },
                enabled = word.isNotBlank() && meaning.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("form_save_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu Thông Tin", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Hủy", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
