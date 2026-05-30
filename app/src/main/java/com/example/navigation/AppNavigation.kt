package com.example.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NamedNavArgument
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
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    val startDestination = if (isUserLoggedIn) "main" else "auth"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        
        // --- Authentication Gate ---
        composable("auth") {
            LoginRegisterScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // --- Core Application Lobby (Standard Tab Navigation) ---
        composable("main") {
            var selectedTab by rememberSaveable { mutableStateOf(0) }
            
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("app_bottom_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { 
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Trang chủ"
                                ) 
                            },
                            label = { Text("Bộ từ") },
                            modifier = Modifier.testTag("nav_tab_home")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { 
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                    contentDescription = "Thống kê"
                                ) 
                            },
                            label = { Text("Tiến trình") },
                            modifier = Modifier.testTag("nav_tab_dashboard")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { 
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                    contentDescription = "Cá nhân"
                                ) 
                            },
                            label = { Text("Hồ sơ") },
                            modifier = Modifier.testTag("nav_tab_profile")
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
                            // Start general review study loop for all sets combined!
                            // Using a special code or set 1 check which we prepopulate
                            viewModel.selectSet(1)
                            navController.navigate("flashcard/1?dueOnly=true")
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
        }

        // --- Vocabulary Detail Pack Listing ---
        composable(
            route = "vocab_detail/{setId}",
            arguments = listOf(navArgument("setId") { type = NavType.IntType })
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getInt("setId") ?: 1
            VocabularyDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddWordNeeded = { sid ->
                    navController.navigate("add_edit_word/$sid")
                },
                onEditWordNeeded = { sid, wid ->
                    navController.navigate("add_edit_word/$sid?wordId=$wid")
                },
                onStartStudy = { sid, dueOnly ->
                    navController.navigate("flashcard/$sid?dueOnly=$dueOnly")
                }
            )
        }

        // --- Add/Edit Word Forms ---
        composable(
            route = "add_edit_word/{setId}?wordId={wordId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.IntType },
                navArgument("wordId") { 
                    type = NavType.StringType // String is safer for null parsing on arguments
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getInt("setId") ?: 1
            val wordIdString = backStackEntry.arguments?.getString("wordId")
            val wordId = wordIdString?.toIntOrNull()

            AddEditWordScreen(
                viewModel = viewModel,
                setId = setId,
                wordId = wordId,
                onBack = { navController.popBackStack() }
            )
        }

        // --- Spaced Repetition Flashcard Loop ---
        composable(
            route = "flashcard/{setId}?dueOnly={dueOnly}",
            arguments = listOf(
                navArgument("setId") { type = NavType.IntType },
                navArgument("dueOnly") { 
                    type = NavType.BoolType
                    defaultValue = false 
                }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getInt("setId") ?: 1
            val dueOnly = backStackEntry.arguments?.getBoolean("dueOnly") ?: false

            FlashcardScreen(
                viewModel = viewModel,
                setId = setId,
                dueOnly = dueOnly,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
