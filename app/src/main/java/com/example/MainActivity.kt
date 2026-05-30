package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.navigation.AppNavigation
import com.example.presentation.VocabularyViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize the shared ViewModel with local dependency Injection
    val viewModel = ViewModelProvider(
        this, 
        VocabularyViewModel.Factory(applicationContext)
    )[VocabularyViewModel::class.java]

    setContent {
      MyApplicationTheme {
        AppNavigation(viewModel = viewModel)
      }
    }
  }
}
