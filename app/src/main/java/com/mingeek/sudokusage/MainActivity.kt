package com.mingeek.sudokusage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}
