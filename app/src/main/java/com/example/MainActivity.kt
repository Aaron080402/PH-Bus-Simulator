package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.model.ScreenState
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimulatorDriveScreen
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BusSimulatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BusSimulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    BusSimulatorRootApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BusSimulatorRootApp(
    viewModel: BusSimulatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(ScreenState.HOME) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpitBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (currentScreen) {
            ScreenState.HOME -> {
                HomeScreen(
                    state = state,
                    availableTerminals = viewModel.availableTerminals,
                    onSelectTerminal = { viewModel.selectStartingTerminal(it) },
                    onStartClick = { currentScreen = ScreenState.DRIVE_SIMULATOR },
                    onAboutClick = { currentScreen = ScreenState.ABOUT },
                    onSettingsClick = { currentScreen = ScreenState.SETTINGS }
                )
            }
            ScreenState.DRIVE_SIMULATOR -> {
                SimulatorDriveScreen(
                    viewModel = viewModel,
                    state = state,
                    onBackToHome = { currentScreen = ScreenState.HOME }
                )
            }
            ScreenState.ABOUT -> {
                AboutScreen(
                    onBackClick = { currentScreen = ScreenState.HOME }
                )
            }
            ScreenState.SETTINGS -> {
                SettingsScreen(
                    state = state,
                    onSetControlMode = { viewModel.setControlMode(it) },
                    onSetForceFeedback = { viewModel.setForceFeedback(it) },
                    onSetWheelSensitivity = { viewModel.setWheelSensitivity(it) },
                    onBackClick = { currentScreen = ScreenState.HOME }
                )
            }
        }
    }
}
