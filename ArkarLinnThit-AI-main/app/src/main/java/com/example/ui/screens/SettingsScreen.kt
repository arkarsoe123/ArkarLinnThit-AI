package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiModelChoice
import com.example.ui.theme.AppThemeMode
import com.example.viewmodel.ArkarUiState

@Composable
fun SettingsScreen(
    uiState: ArkarUiState,
    onUpdateAiModel: (AiModelChoice) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onUpdateApiEndpoint: (String) -> Unit,
    onUpdateAiModelName: (String) -> Unit,
    onUpdateWakeWord: (Boolean, String) -> Unit,
    onUpdateVoiceFeedback: (Boolean) -> Unit,
    onUpdateThemeMode: (AppThemeMode) -> Unit,
    onUpdateToolEnabled: (String, Boolean) -> Unit,
    onUpdateRequireConfirmation: (Boolean) -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenDeveloper: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var modelMenu by remember { mutableStateOf(false) }
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Column { Text("Settings", fontSize=28.sp); Text("AI · Capabilities · Voice · Privacy", color=androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant) }; TextButton(onClick=onBack){Text("Done")} } }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("AI model", style=androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick={modelMenu=true}, modifier=Modifier.fillMaxWidth()){Text(uiState.aiModel.displayName)}
                    DropdownMenu(expanded=modelMenu,onDismissRequest={modelMenu=false}) { AiModelChoice.entries.forEach { m -> DropdownMenuItem(text={Text(m.displayName)},onClick={onUpdateAiModel.also { onUpdateAiModel(m); modelMenu=false }}) } }
                    Spacer(Modifier.height(8.dp))
                    Text(uiState.aiModel.description, fontSize=12.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value=uiState.customApiKey,onValueChange=onUpdateApiKey,label={Text("API key (optional)")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value=uiState.apiEndpoint,onValueChange=onUpdateApiEndpoint,label={Text("Endpoint (optional)")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value=uiState.aiModelName,onValueChange=onUpdateAiModelName,label={Text("Model name (optional)")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("API key ကို source code ထဲမှာ မထည့်ထားပါ။ ဒီ device မှာ user က ကိုယ်တိုင် configure လုပ်ရန်သာ ဖြစ်ပါတယ်။", fontSize=11.sp)
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Capabilities", style=androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Tool အားလုံးကို user က စိတ်ကြိုက်ဖွင့်/ပိတ်နိုင်ပါတယ်။", fontSize=12.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick=onOpenCapabilities, modifier=Modifier.fillMaxWidth()){Text("Manage tools")}
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) { Text("Sensitive actions confirmation"); Switch(checked=uiState.requireConfirmation,onCheckedChange=onUpdateRequireConfirmation) }
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Developer & Support", style=androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Mr.A · Arkar Lin Thit AI", fontSize=13.sp)
                    Text("KPay support, app information နှင့် project developer page", fontSize=11.sp, color=androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick=onOpenDeveloper, modifier=Modifier.fillMaxWidth()) { Text("Developer Mr.A / Support") }
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Voice", style=androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ Text("Wake word"); Switch(checked=uiState.wakeWordEnabled,onCheckedChange={onUpdateWakeWord(it,uiState.wakeWordPhrase)}) }
                    OutlinedTextField(value=uiState.wakeWordPhrase,onValueChange={onUpdateWakeWord(uiState.wakeWordEnabled,it)},label={Text("Wake phrase")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ Text("Voice feedback"); Switch(checked=uiState.voiceFeedbackEnabled,onCheckedChange=onUpdateVoiceFeedback) }
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Appearance", style=androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    AppThemeMode.entries.forEach { mode -> TextButton(onClick={onUpdateThemeMode(mode)},modifier=Modifier.fillMaxWidth()){Text(if(uiState.themeMode==mode) "✓ ${mode.name}" else mode.name)} }
                }
            }
        }
    }
}
