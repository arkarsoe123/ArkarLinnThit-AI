package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.DynamicUiCard
import com.example.viewmodel.ArkarUiState

private val Bg = Color(0xFF0B0C0F)
private val Surface1 = Color(0xFF15171B)
private val Surface2 = Color(0xFF1D2026)
private val Accent = Color(0xFF6C8CFF)

@Composable
fun ChatFirstScreen(
    uiState: ArkarUiState,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewChat: () -> Unit,
    onOpenTool: (com.example.model.ActiveToolScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(uiState.chatMessages.size) { if (uiState.chatMessages.isNotEmpty()) listState.animateScrollToItem(uiState.chatMessages.lastIndex) }

    Column(modifier.fillMaxSize().background(Bg).imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Accent.copy(alpha = .18f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = Accent) }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("အာကာလင်းသစ် AI", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(uiState.aiModel.displayName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            Row {
                IconButton(onClick = onNewChat) { Icon(Icons.Default.Add, "New chat") }
                IconButton(onClick = { onOpenTool(com.example.model.ActiveToolScreen.SMART_AUTOMATION) }) { Icon(Icons.Default.GridView, "Tools") }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, "Settings") }
            }
        }

        if (uiState.chatMessages.size <= 1) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(shape = CircleShape, color = Surface2, modifier = Modifier.size(72.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(34.dp)) } }
                Spacer(Modifier.height(16.dp))
                Text("မင်္ဂလာပါ 👋", fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("ဘာလုပ်ပေးရမလဲ?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                Spacer(Modifier.height(20.dp))
                Suggestion("ဖုန်းမီး ဖွင့်ပေး") { onPromptChange("ဖုန်းမီး ဖွင့်ပေး") }
                Suggestion("ဒီနေ့ လုပ်စရာစာရင်းလုပ်ပေး") { onPromptChange("ဒီနေ့ လုပ်စရာစာရင်းလုပ်ပေး") }
                Suggestion("/cmd help") { onPromptChange("/cmd help") }
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 10.dp)) {
                items(uiState.chatMessages) { message ->
                    MessageBubble(message, uiState.dynamicUiCards, onOpenTool)
                }
            }
        }

        Composer(uiState, onPromptChange, onSend, onVoice)
    }
}

@Composable private fun Suggestion(text: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Surface1, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Text(text, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), fontSize = 13.sp)
    }
}

@Composable private fun MessageBubble(message: ChatMessage, cards: List<DynamicUiCard>, onOpenTool: (com.example.model.ActiveToolScreen) -> Unit) {
    val isUser = message.sender == ChatSender.USER
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        Surface(shape = RoundedCornerShape(18.dp), color = if (isUser) Accent.copy(alpha = .18f) else Surface1, modifier = Modifier.fillMaxWidth(if (isUser) .86f else .94f).border(1.dp, Color.White.copy(alpha=.05f), RoundedCornerShape(18.dp))) {
            Text(message.text, modifier = Modifier.padding(13.dp), fontSize = 14.sp, lineHeight = 21.sp)
        }
        if (!isUser && message.toolTriggered != null) {
            Spacer(Modifier.height(6.dp))
            val card = cards.lastOrNull()
            if (card != null) ResultCard(card) { onOpenTool(message.toolTriggered) }
        }
    }
}

@Composable private fun ResultCard(card: DynamicUiCard, onOpen: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Surface2, modifier = Modifier.fillMaxWidth(.94f).border(1.dp, Accent.copy(alpha=.28f), RoundedCornerShape(18.dp))) {
        Column(Modifier.padding(14.dp)) {
            Text(card.title, fontWeight = FontWeight.SemiBold)
            Text(card.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(card.primaryMetric, color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(card.secondaryMetric, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(card.statusText, fontSize = 12.sp)
            if (card.items.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                card.items.take(3).forEach { Text("• $it", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp) }
            }
            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = Accent.copy(alpha=.16f), modifier = Modifier.clickable(onClick = onOpen)) {
                Text("Open / Continue", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(horizontal=12.dp, vertical=8.dp))
            }
        }
    }
}

@Composable private fun Composer(uiState: ArkarUiState, onChange: (String)->Unit, onSend:()->Unit, onVoice:()->Unit) {
    Surface(color = Bg, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(
                value = uiState.promptInput,
                onValueChange = onChange,
                placeholder = { Text("Message Arkar…", color = Color(0xFF707784)) },
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF2A2E35)),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onVoice, modifier = Modifier.size(46.dp).background(Surface2, CircleShape)) { Icon(if (uiState.isListeningVoice) Icons.Default.Stop else Icons.Default.Mic, "Voice", tint = if (uiState.isListeningVoice) Color(0xFF4ADE80) else Color.White) }
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onSend, modifier = Modifier.size(46.dp).background(Accent, CircleShape)) { Icon(Icons.Default.Send, "Send", tint = Color.White) }
        }
    }
}
