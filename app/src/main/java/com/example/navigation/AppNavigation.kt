package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.presentation.VocabularyViewModel
import com.example.presentation.auth.LoginRegisterScreen
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.detail.VocabularyDetailScreen
import com.example.presentation.edit.AddEditWordScreen
import com.example.presentation.flashcard.FlashcardScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.profile.ProfileScreen

@Composable
fun AppNavigation(
    viewModel: VocabularyViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    // Nullable to handle initial loading state from DataStore
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    if (isLoggedIn == null) {
        SplashScreen()
    } else {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn == true) "main" else "auth",
            modifier = modifier.fillMaxSize()
        ) {
            
            // --- Authentication Gate ---
            composable("auth") {
                LoginRegisterScreen(
                    vocabularyViewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // --- Core Application Lobby ---
            composable("main") {
                var selectedTab by rememberSaveable { mutableStateOf(0) }
                
                Scaffold(
                    bottomBar = {
                        NavigationBar(modifier = Modifier.testTag("app_bottom_bar")) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { 
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = null
                                    ) 
                                },
                                label = { Text("Bộ từ") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { 
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                        contentDescription = null
                                    ) 
                                },
                                label = { Text("Tiến trình") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { 
                                    Icon(
                                        imageVector = if (selectedTab == 2) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                        contentDescription = null
                                    ) 
                                },
                                label = { Text("Hồ sơ") }
                            )
                        }
                    }
                ) { paddingValues ->
                    val innerModifier = Modifier.padding(paddingValues)
                    when (selectedTab) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onSetSelected = { setId ->
                                viewModel.selectSet(setId)
                                navController.navigate("vocab_detail/$setId")
                            },
                            onStudyDue = {
                                viewModel.selectSet(1)
                                navController.navigate("flashcard/1?dueOnly=true")
                            },
                            modifier = innerModifier
                        )
                        1 -> DashboardScreen(viewModel = viewModel, modifier = innerModifier)
                        2 -> ProfileScreen(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout()
                                navController.navigate("auth") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            modifier = innerModifier
                        )
                    }
                }
            ) { paddingValues ->
                val innerModifier = Modifier.padding(paddingValues)
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onSetSelected = { setId ->
                            viewModel.selectSet(setId)
                            navController.navigate("vocab_detail/$setId")
                        },
                        onStudyDue = {
                            // Start general review study loop for ALL due words across all sets!
                            viewModel.selectSet(-1)
                            navController.navigate("flashcard/-1?dueOnly=true")
                        },
                        modifier = innerModifier
                    )
                    1 -> DashboardScreen(
                        viewModel = viewModel,
                        modifier = innerModifier
                    )
                    2 -> ProfileScreen(
                        viewModel = viewModel,
                        onLogout = {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        },
                        modifier = innerModifier
                    )
                }
            }

            composable(
                route = "vocab_detail/{setId}",
                arguments = listOf(navArgument("setId") { type = NavType.IntType })
            ) { backStackEntry ->
                val setId = backStackEntry.arguments?.getInt("setId") ?: 1
                VocabularyDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddWordNeeded = { sid -> navController.navigate("add_edit_word/$sid") },
                    onEditWordNeeded = { sid, wid -> navController.navigate("add_edit_word/$sid?wordId=$wid") },
                    onStartStudy = { sid, dueOnly -> navController.navigate("flashcard/$sid?dueOnly=$dueOnly") }
                )
            }

            composable(
                route = "add_edit_word/{setId}?wordId={wordId}",
                arguments = listOf(
                    navArgument("setId") { type = NavType.IntType },
                    navArgument("wordId") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val setId = backStackEntry.arguments?.getInt("setId") ?: 1
                val wordId = backStackEntry.arguments?.getString("wordId")?.toIntOrNull()
                AddEditWordScreen(viewModel = viewModel, setId = setId, wordId = wordId, onBack = { navController.popBackStack() })
            }

            composable(
                route = "flashcard/{setId}?dueOnly={dueOnly}",
                arguments = listOf(
                    navArgument("setId") { type = NavType.IntType },
                    navArgument("dueOnly") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val setId = backStackEntry.arguments?.getInt("setId") ?: 1
                val dueOnly = backStackEntry.arguments?.getBoolean("dueOnly") ?: false
                FlashcardScreen(viewModel = viewModel, setId = setId, dueOnly = dueOnly, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MinLish",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = Color.White)
        }
    }
}
