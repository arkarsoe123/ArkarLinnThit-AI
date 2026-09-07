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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerStopwatchScreen(
    timerSecondsRemaining: Int,
    isTimerRunning: Boolean,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: (Int) -> Unit,
    stopwatchTimeMs: Long,
    isStopwatchRunning: Boolean,
    stopwatchLaps: List<String>,
    onToggleStopwatch: () -> Unit,
    onLapStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Timer, 1: Stopwatch

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "တိုင်မာ & စက္ကန့်နာရီ",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = "High Precision Chronometer",
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF00F0FF))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Selector (Timer / Stopwatch)
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 0) Color(0xFF00F0FF) else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 0 }
            ) {
                Text(
                    text = "တိုင်မာ (Timer)",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedTab == 1) Color(0xFF00F0FF) else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 1 }
            ) {
                Text(
                    text = "စက္ကန့်နာရီ (Stopwatch)",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.Black else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedTab == 0) {
            // COUNTDOWN TIMER TAB
            val min = timerSecondsRemaining / 60
            val sec = timerSecondsRemaining % 60
            val formattedTime = "%02d:%02d".format(min, sec)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                CircularProgressIndicator(
                    progress = { (timerSecondsRemaining / 300f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(220.dp),
                    strokeWidth = 10.dp,
                    color = Color(0xFF00F0FF),
                    trackColor = Color(0xFF1E293B)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = Color(0xFF00F0FF)
                        )
                    )
                    Text(
                        text = if (isTimerRunning) "အလုပ်လုပ်နေသည်..." else "အသင့်ဖြစ်သည်",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(60 to "1 မိနစ်", 180 to "3 မိနစ်", 300 to "5 မိနစ်", 1500 to "25 မိနစ် (Pomodoro)").forEach { (s, label) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onResetTimer(s) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Timer Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onResetTimer(180) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset Timer", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(20.dp))

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF00F0FF),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(72.dp)
                        .clickable { if (isTimerRunning) onPauseTimer() else onStartTimer() }
                        .testTag("timer_start_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Start/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        } else {
            // STOPWATCH TAB
            val sec = (stopwatchTimeMs / 1000) % 60
            val min = (stopwatchTimeMs / (1000 * 60)) % 60
            val hundredths = (stopwatchTimeMs % 1000) / 10
            val formattedStopwatch = "%02d:%02d.%02d".format(min, sec, hundredths)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .border(1.5.dp, Color(0xFF8B5CF6), RoundedCornerShape(20.dp))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = formattedStopwatch,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 46.sp,
                            color = Color(0xFF00F0FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stopwatch Controls: Reset, Start/Pause, Lap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onResetStopwatch,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset Stopwatch", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(18.dp))

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF8B5CF6),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .clickable { onToggleStopwatch() }
                        .testTag("stopwatch_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Start/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Button(
                    onClick = onLapStopwatch,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Flag, contentDescription = "Lap", tint = Color(0xFF00F0FF))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Laps List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(110.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                items(stopwatchLaps.reversed()) { lap ->
                    Text(
                        text = lap,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0)),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("ပင်မသို့ ပြန်သွားမည်", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
