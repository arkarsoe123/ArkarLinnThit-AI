package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.ChatSender
import kotlin.math.roundToInt

@Composable
fun FloatingChatPopup(
    isVisible: Boolean,
    isMinimized: Boolean,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onToggleVoice: () -> Unit,
    isVoiceListening: Boolean,
    onClose: () -> Unit,
    onToggleMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(20f) }
    var offsetY by remember { mutableFloatStateOf(100f) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(0f, 600f)
                        offsetY = (offsetY + dragAmount.y).coerceIn(0f, 1200f)
                    }
                }
        ) {
            if (isMinimized) {
                // Minimized Floating Mascot Bubble (Sleek Interface)
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1D2024),
                    shadowElevation = 14.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.5.dp, Color(0xFF3B82F6), CircleShape)
                        .clickable { onToggleMinimize() }
                        .testTag("floating_chat_minimized_bubble")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Expand Arkar Chat",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                // Expanded Floating Chat Box Window (Sleek Interface)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1D2024).copy(alpha = 0.98f),
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .width(320.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        .testTag("floating_chat_window")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenWith,
                                contentDescription = "Drag Window",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "အာကာလင်းသစ် Popup Chat",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFF60A5FA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onToggleMinimize,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandLess,
                                    contentDescription = "Minimize",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Floating Chat",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chat Messages List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF16181B))
                                .padding(8.dp)
                        ) {
                            items(messages.takeLast(10)) { msg ->
                                val isUser = msg.sender == ChatSender.USER
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isUser) Color(0xFF2563EB) else Color(0xFF2D3135),
                                        modifier = Modifier.widthIn(max = 220.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFE2E2E6),
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Input Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        "မေးမြန်းလိုရာ ရေးပါ...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                    focusedTextColor = Color(0xFFE2E2E6),
                                    unfocusedTextColor = Color(0xFFE2E2E6),
                                    focusedContainerColor = Color(0xFF2D3135),
                                    unfocusedContainerColor = Color(0xFF2D3135)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("floating_chat_input")
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = onToggleVoice,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isVoiceListening) Color(0xFF22C55E) else Color(0xFF2D3135),
                                        CircleShape
                                    )
                                    .testTag("floating_voice_button")
                            ) {
                                Icon(
                                    imageVector = if (isVoiceListening) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = "Voice Input",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        onSendMessage(textInput)
                                        textInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(Color(0xFF3B82F6), Color(0xFFA855F7))
                                        ),
                                        CircleShape
                                    )
                                    .testTag("floating_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Message",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.widthIn(max: androidx.compose.ui.unit.Dp): Modifier =
    this.padding(horizontal = 2.dp)
