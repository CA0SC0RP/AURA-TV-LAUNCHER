package com.example

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TvLauncherScreen
import com.example.ui.TvLauncherViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TvBackground

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Force landscape mode for Android TV / 4K widescreen
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    enableEdgeToEdge()

    // Immersive TV sticky full-screen mode
    hideSystemBars()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        val launcherViewModel: TvLauncherViewModel = viewModel()
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = TvBackground
        ) {
          TvLauncherScreen(viewModel = launcherViewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    hideSystemBars()
  }

  private fun hideSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.insetsController?.let { controller ->
        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    } else {
      @Suppress("DEPRECATION")
      window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          or View.SYSTEM_UI_FLAG_FULLSCREEN
          or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
          or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      )
    }
  }
}
