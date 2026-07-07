package com.mingeek.sudokusage

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.audio.AudioController
import com.mingeek.sudokusage.audio.BgmTrack
import com.mingeek.sudokusage.data.preferences.GameplayPrefs
import com.mingeek.sudokusage.data.preferences.GameplaySettings
import com.mingeek.sudokusage.data.preferences.ThemeChoice
import com.mingeek.sudokusage.ui.navigation.SudokuNavGraph
import com.mingeek.sudokusage.ui.theme.SudokuSageTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val audio: AudioController by inject()
    private val gameplaySettings: GameplaySettings by inject()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* outcome ignored — app works regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only on a fresh process start, not on every config-change recreation —
        // POST_NOTIFICATIONS only allows two visible prompts before becoming
        // permanent-deny, and a couple of rotations would burn both.
        if (savedInstanceState == null) maybeRequestNotificationPermission()

        setContent {
            val prefs by gameplaySettings.state
                .collectAsStateWithLifecycle(initialValue = GameplayPrefs())
            val systemDark = isSystemInDarkTheme()
            val (darkTheme, amoled) = when (prefs.theme) {
                ThemeChoice.System -> systemDark to false
                ThemeChoice.Light -> false to false
                ThemeChoice.Dark -> true to false
                ThemeChoice.Amoled -> true to true
            }
            SudokuSageTheme(darkTheme = darkTheme, amoled = amoled, palette = prefs.palette) {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        SudokuNavGraph()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audio.onAppStart()
        audio.playBgm(BgmTrack.Menu)
    }

    override fun onStop() {
        audio.onAppStop()
        super.onStop()
    }

    private fun maybeRequestNotificationPermission() {
        // POST_NOTIFICATIONS only exists on API 33+; older devices need no permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
