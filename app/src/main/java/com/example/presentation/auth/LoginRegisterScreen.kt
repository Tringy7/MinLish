package com.example.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.EnglishLevel
import com.example.presentation.VocabularyViewModel
import com.example.presentation.components.MinLishCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    vocabularyViewModel: VocabularyViewModel, 
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(context)
    )
    
    val uiState by loginViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Constants
    val cefrLevels = EnglishLevel.entries
    val studyGoals = listOf("IELTS", "TOEIC", "Giao tiếp", "Công việc", "Du học", "Khác")

    // --- Google Sign-In Logic ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                loginViewModel.googleSignIn(
                    email = account.email ?: "",
                    displayName = account.displayName ?: "",
                    avatarUrl = account.photoUrl?.toString() ?: ""
                )
            }
        } catch (e: ApiException) {
            scope.launch { 
                snackbarHostState.showSnackbar("Đăng nhập Google thất bại: ${e.statusCode}") 
            }
        }
    }

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var englishLevel by remember { mutableStateOf(EnglishLevel.B1) }
    var learningGoal by remember { mutableStateOf("Giao tiếp") }

    // Navigation and Error handling
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        } else if (uiState is LoginUiState.Error) {
            snackbarHostState.showSnackbar((uiState as LoginUiState.Error).message)
            loginViewModel.resetState()
        }
    }

    // Setup Dialog for Google First-timers
    if (uiState is LoginUiState.RequireSetup) {
        var setupLevel by remember { mutableStateOf(EnglishLevel.B1) }
        var setupGoal by remember { mutableStateOf("Giao tiếp") }

        AlertDialog(
            onDismissRequest = { loginViewModel.resetState() },
            title = { Text("Hoàn thiện hồ sơ") },
            text = {
                Column {
                    Text("Chào mừng! Hãy cho chúng tôi biết trình độ và mục tiêu của bạn.")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Level Dropdown
                    Text("Trình độ hiện tại (CEFR):", style = MaterialTheme.typography.bodySmall)
                    var levelExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = levelExp, onExpandedChange = { levelExp = it }) {
                        OutlinedTextField(
                            value = setupLevel.fullLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExp) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = levelExp, onDismissRequest = { levelExp = false }) {
                            cefrLevels.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.fullLabel) },
                                    onClick = { setupLevel = level; levelExp = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Goal Dropdown
                    Text("Mục tiêu học tập:", style = MaterialTheme.typography.bodySmall)
                    var goalExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = goalExp, onExpandedChange = { goalExp = it }) {
                        OutlinedTextField(
                            value = setupGoal,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExp) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = goalExp, onDismissRequest = { goalExp = false }) {
                            studyGoals.forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal) },
                                    onClick = { setupGoal = goal; goalExp = false }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { loginViewModel.completeSetup(setupLevel, setupGoal) }) {
                    Text("Bắt đầu ngay")
                }
            }
        )
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Branding
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(44.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "MinLish", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp), color = MaterialTheme.colorScheme.primary)
                Text(text = "Learn Smartly with Spaced Repetition", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(32.dp))

                MinLishCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = if (isSignUp) "Đăng Ký Tài Khoản" else "Đăng Nhập Hệ Thống",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isSignUp) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Họ và tên") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Địa chỉ Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isSignUp) {
                        OutlinedTextField(
                            value = confirmPassword, onValueChange = { confirmPassword = it },
                            label = { Text("Xác nhận mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Level selection
                        var levelExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = levelExpanded, onExpandedChange = { levelExpanded = it }) {
                            OutlinedTextField(
                                value = "Trình độ: ${englishLevel.fullLabel}",
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor().padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = levelExpanded, onDismissRequest = { levelExpanded = false }) {
                                cefrLevels.forEach { level ->
                                    DropdownMenuItem(
                                        text = { Text(level.fullLabel) }, 
                                        onClick = { englishLevel = level; levelExpanded = false }
                                    )
                                }
                            }
                        }

                        // Goal selection
                        var goalExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = goalExpanded, onExpandedChange = { goalExpanded = it }) {
                            OutlinedTextField(
                                value = "Mục tiêu: $learningGoal",
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = goalExpanded, onDismissRequest = { goalExpanded = false }) {
                                studyGoals.forEach { goal ->
                                    DropdownMenuItem(text = { Text(goal) }, onClick = { learningGoal = goal; goalExpanded = false })
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isSignUp) {
                                if (password != confirmPassword) {
                                    scope.launch { snackbarHostState.showSnackbar("Mật khẩu xác nhận không khớp") }
                                    return@Button
                                }
                                loginViewModel.signUp(email, name, password, englishLevel, learningGoal)
                            } else {
                                loginViewModel.login(email, password)
                            }
                        },
                        enabled = uiState !is LoginUiState.Loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text(text = if (isSignUp) "Tạo Tài Khoản" else "Đăng Nhập", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(text = " Hoặc đăng nhập bằng ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { launcher.launch(googleSignInClient.signInIntent) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFEA4335)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF4285F4)))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Đăng nhập với Google", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { isSignUp = !isSignUp; loginViewModel.resetState() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = if (isSignUp) "Đã có tài khoản? Đăng nhập ngay" else "Chưa có tài khoản? Nhấn đăng ký", color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
