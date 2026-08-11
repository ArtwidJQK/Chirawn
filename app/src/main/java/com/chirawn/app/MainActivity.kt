package com.chirawn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import com.chirawn.app.data.AppDatabase
import com.chirawn.app.data.HubRepository

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val repo = HubRepository(AppDatabase.create(applicationContext).dao())
  setContent { MaterialTheme(colorScheme = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF5B5BD6), secondary = androidx.compose.ui.graphics.Color(0xFF427E75), background = androidx.compose.ui.graphics.Color(0xFFFAF9FF))) { ChirawnApp(repo) } }
 }
}
