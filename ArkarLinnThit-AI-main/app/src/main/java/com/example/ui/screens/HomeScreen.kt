package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveToolScreen
import com.example.ui.components.ArkarMascot
import com.example.viewmodel.ArkarUiState

private val HomeBg = Color(0xFF0F1113)
private val CardBg = Color(0xFF191C20)
private val CardBg2 = Color(0xFF20242A)
private val PrimaryText = Color(0xFFF3F4F6)
private val SecondaryText = Color(0xFF9CA3AF)
private val Accent = Color(0xFF4F8CFF)

@Composable
fun HomeScreen(
    uiState: ArkarUiState,
    onPromptChange: (String) -> Unit,
    onSubmitPrompt: () -> Unit,
    onToggleVoice: () -> Unit,
    onSelectTool: (ActiveToolScreen) -> Unit,
    onMascotClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBg),
        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ArkarMascot(
                emotion = uiState.mascotEmotion,
                latestSpeechText = null,
                onMascotClick = onMascotClick,
                size = 104.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "မင်္ဂလာပါ 👋",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText,
                    fontSize = 25.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "ဘာလုပ်ပေးရမလဲ?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = SecondaryText,
                    fontSize = 15.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp),
                textAlign = TextAlign.Center
            )

        }

        item {
            PromptInputCardV2(
                text = uiState.promptInput,
                onTextChange = onPromptChange,
                onSubmit = onSubmitPrompt,
                isListening = uiState.isListeningVoice,
                onToggleListening = onToggleVoice
            )
        }

        item {
            SectionTitle(
                title = "Quick actions",
                action = "အားလုံးကြည့်ရန်",
                onAction = { onSelectTool(ActiveToolScreen.SMART_AUTOMATION) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Mic,
                    title = "Voice",
                    subtitle = if (uiState.isListeningVoice) "Listening" else "ပြောမယ်",
                    active = uiState.isListeningVoice,
                    onClick = onToggleVoice
                )
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Description,
                    title = "Notes",
                    subtitle = "မှတ်စုရေး",
                    onClick = { onSelectTool(ActiveToolScreen.NOTES) }
                )
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.GridView,
                    title = "Tools",
                    subtitle = "Tools အားလုံး",
                    onClick = { onSelectTool(ActiveToolScreen.SMART_AUTOMATION) }
                )
            }
        }

        if (uiState.chatMessages.isNotEmpty()) {
            item {
                val last = uiState.chatMessages.last()
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.06f),
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "နောက်ဆုံးဆက်သွယ်မှု",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = last.text.take(180) + if (last.text.length > 180) "…" else "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PrimaryText,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptInputCardV2(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isListening: Boolean,
    onToggleListening: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(22.dp))
            .testTag("prompt_input_card")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        "ဘာကိုလုပ်ပေးရမလဲ…",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                },
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = Accent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_prompt_textfield")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isListening) "နားထောင်နေပါတယ်…" else "စာရေးပါ သို့မဟုတ် အသံနဲ့ပြောပါ",
                    color = if (isListening) Color(0xFF4ADE80) else SecondaryText,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleListening,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isListening) Color(0xFF22C55E) else CardBg2,
                            CircleShape
                        )
                        .testTag("prompt_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Voice Input",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onSubmit,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Accent, CircleShape)
                        .testTag("prompt_submit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                color = Accent,
                fontSize = 11.sp,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (active) Color(0xFF1D3B2A) else CardBg,
        modifier = modifier
            .height(86.dp)
            .border(
                1.dp,
                if (active) Color(0xFF22C55E).copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (active) Color(0xFF4ADE80) else Accent,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    color = PrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = SecondaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun MiniToolPill(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(15.dp),
        color = CardBg,
        modifier = modifier
            .height(48.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(15.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Accent,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = PrimaryText, fontSize = 11.sp)
        }
    }
}
