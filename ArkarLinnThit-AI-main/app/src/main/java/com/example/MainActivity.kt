package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ActiveToolScreen
import com.example.ui.components.FloatingChatPopup
import com.example.ui.components.ToolAnimationHud
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.AiSkillLibraryScreen
import com.example.ui.screens.DeveloperSupportScreen
import com.example.ui.screens.ChatFirstScreen
import com.example.ui.screens.CodeEditorScreen
import com.example.ui.screens.DynamicGeneratedUiScreen
import com.example.ui.screens.FlashlightScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.PhoneCallScreen
import com.example.ui.screens.PhotoSlideShowScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartAutomationScreen
import com.example.ui.screens.SpreadsheetScreen
import com.example.ui.screens.TimerStopwatchScreen
import com.example.ui.screens.ToolCatalogScreen
import com.example.ui.screens.WifiSystemScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ArkarMainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: ArkarMainViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) vm.toggleVoiceListening() }
            MyApplicationTheme(themeMode=state.themeMode) {
                ArkarAppRoot(vm, requestMic={
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) vm.toggleVoiceListening() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }, onExit = { finish() })
            }
        }
    }
}

@Composable
fun ArkarAppRoot(vm: ArkarMainViewModel, requestMic: ()->Unit, onExit: () -> Unit) {
    val s by vm.uiState.collectAsState()
    val chat = s.activeScreen == ActiveToolScreen.HOME
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (!chat) vm.selectScreen(ActiveToolScreen.HOME) else showExitDialog = true
    }
    Box(Modifier.fillMaxSize().background(Color(0xFF0B0C0F))) {
            when (s.activeScreen) {
                ActiveToolScreen.HOME -> ChatFirstScreen(s, vm::updatePromptInput, {vm.sendUserPrompt()}, requestMic, {vm.selectScreen(ActiveToolScreen.SETTINGS)}, {vm.newChat()}, vm::selectScreen)
                ActiveToolScreen.SMART_AUTOMATION -> ToolCatalogScreen(
                    s.enabledTools, vm::updateToolEnabled, vm::selectScreen,
                    {vm.selectScreen(ActiveToolScreen.SETTINGS)},
                    {vm.selectScreen(ActiveToolScreen.AI_SKILLS)}
                )
                ActiveToolScreen.AI_SKILLS -> AiSkillLibraryScreen(
                    onUseSkill = { prompt -> vm.updatePromptInput(prompt); vm.selectScreen(ActiveToolScreen.HOME) },
                    onBack = { vm.selectScreen(ActiveToolScreen.SMART_AUTOMATION) }
                )
                ActiveToolScreen.DEVELOPER_SUPPORT -> DeveloperSupportScreen { vm.selectScreen(ActiveToolScreen.SETTINGS) }
                ActiveToolScreen.SETTINGS -> SettingsScreen(
                    s, vm::updateAiModel, vm::updateCustomApiKey, vm::updateApiEndpoint, vm::updateAiModelName,
                    {e,p->vm.updateWakeWord(e,p)}, vm::updateVoiceFeedback, vm::updateThemeMode,
                    vm::updateToolEnabled, vm::updateRequireConfirmation,
                    {vm.selectScreen(ActiveToolScreen.SMART_AUTOMATION)},
                    {vm.selectScreen(ActiveToolScreen.DEVELOPER_SUPPORT)},
                    {vm.selectScreen(ActiveToolScreen.HOME)}
                )
                ActiveToolScreen.FLASHLIGHT -> FlashlightScreen(s.isTorchActive,{vm.toggleTorch()},{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.CAMERA -> CameraScreen({vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.MUSIC_PLAYER -> MusicPlayerScreen({vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.PHOTO_SLIDESHOW -> PhotoSlideShowScreen({vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.TIMER_STOPWATCH -> TimerStopwatchScreen(s.timerSecondsRemaining,s.isTimerRunning,{vm.startTimer()},{vm.pauseTimer()},{vm.resetTimer(it)},s.stopwatchTimeMs,s.isStopwatchRunning,s.stopwatchLaps,{vm.toggleStopwatch()},{vm.lapStopwatch()},{vm.resetStopwatch()},{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.PHONE_CALL -> PhoneCallScreen(s.dialedNumber,vm::appendDialDigit,vm::deleteDialDigit,vm::clearDialNumber,vm::dialNumber,{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.NOTES -> NotesScreen(s.notes,{t,c,tag->vm.addNote(t,c,tag)},vm::deleteNote,{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.SPREADSHEET -> SpreadsheetScreen(s.spreadsheetCells,{k,v->vm.updateSpreadsheetCell(k,v)},vm::clearSpreadsheet,{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.CODE_EDITOR -> CodeEditorScreen(s.codeFiles,s.activeFileIndex,s.terminalLogs,s.isTerminalOpen,s.recentFileNames,vm::selectCodeFile,vm::updateActiveFileContent,vm::saveActiveCodeFile,{n,l->vm.newCodeFile(n,l)},vm::runActiveCode,vm::toggleTerminal,{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.WIFI_SYSTEM -> WifiSystemScreen(vm::openWifiSettings,vm::openQuickShare,{vm.selectScreen(ActiveToolScreen.HOME)})
                ActiveToolScreen.DYNAMIC_GENERATED_UI -> DynamicGeneratedUiScreen(s.dynamicUiCards,vm::generateFreshDynamicUi,{vm.selectScreen(ActiveToolScreen.HOME)})
                else -> ChatFirstScreen(s, vm::updatePromptInput, {vm.sendUserPrompt()}, requestMic, {vm.selectScreen(ActiveToolScreen.SETTINGS)}, {vm.newChat()}, vm::selectScreen)
            }
            ToolAnimationHud(s.toolTransitionNotice,s.animatedToolChoice,Modifier)
            FloatingChatPopup(s.isFloatingChatVisible,s.isFloatingChatMinimized,s.chatMessages,{vm.sendUserPrompt(it)},requestMic,s.isListeningVoice,{vm.toggleFloatingChat()},{vm.minimizeFloatingChat(!s.isFloatingChatMinimized)})

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("အက်ပ်မှ ထွက်မလား?") },
                    text = { Text("အာကာလင်းသစ် AI ကို ပိတ်လိုပါသလား?") },
                    confirmButton = { TextButton(onClick = { showExitDialog = false; onExit() }) { Text("ထွက်မည်") } },
                    dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("မလုပ်တော့") } }
                )
            }
        }
    }
